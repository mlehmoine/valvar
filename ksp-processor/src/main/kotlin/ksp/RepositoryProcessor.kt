// ksp-processor/src/main/kotlin/ksp/RepositoryProcessor.kt
package ksp

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toTypeName
import org.jetbrains.exposed.sql.*

class RepositoryProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(GenerateRepository::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.validate() }

        symbols.forEach { generateRepository(it) }
        return symbols.filterNot { it.validate() }.toList()
    }

    private fun generateRepository(dtoClass: KSClassDeclaration) {
        val packageName = dtoClass.packageName.asString()
        val dtoName = dtoClass.simpleName.asString()
        val repositoryName = "${dtoName}Repository"
        val annotation = dtoClass.annotations
            .find { it.shortName.asString() == "GenerateRepository" }
            ?: return logger.error("No GenerateRepository annotation on $dtoName")
        val tableClass = annotation.arguments
            .find { it.name?.asString() == "tableClass" }
            ?.value as? KSClassDeclaration
            ?: return logger.error("No tableClass specified for $dtoName")
        val tableName = tableClass.simpleName.asString()
        val tablePackage = tableClass.packageName.asString()

        // Get DTO properties (excluding id)
        val properties = dtoClass.getAllProperties()
            .filter { it.simpleName.asString() != "id" }
            .map { it.simpleName.asString() to it.type.resolve().toTypeName() }
            .toList()

        // Generate repository class with MapStruct mapper
        val fileSpec = FileSpec.builder(packageName, repositoryName)
            .addImport(tablePackage, tableName)
            .addImport("org.jetbrains.exposed.sql", "")
            .addImport("java.sql", "PreparedStatement")
            .addImport("java.sql", "ResultSet")
            .addType(
                TypeSpec.interfaceBuilder("${dtoName}Mapper")
                    .addAnnotation(
                        AnnotationSpec.builder(ClassName("org.mapstruct", "Mapper"))
                            .addMember("componentModel = %S", "default")
                            .build()
                    )
                    .addFunction(
                        FunSpec.builder("toDto")
                            .addParameter("row", ClassName("org.jetbrains.exposed.sql", "ResultRow"))
                            .returns(ClassName(packageName, dtoName))
                            .addModifiers(KModifier.ABSTRACT)
                            .build()
                    )
                    .build()
            )
            .addType(
                TypeSpec.classBuilder(repositoryName)
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameter("mapper", ClassName(packageName, "${dtoName}Mapper"))
                            .build()
                    )
                    .addProperty(
                        PropertySpec.builder("mapper", ClassName(packageName, "${dtoName}Mapper"))
                            .initializer("mapper")
                            .addModifiers(KModifier.PRIVATE)
                            .build()
                    )
                    .addFunction(
                        FunSpec.builder("findById")
                            .addParameter("id", Long::class)
                            .returns(ClassName(packageName, dtoName).copy(nullable = true))
                            .addStatement(
                                "return %L.select { %L.id eq id }.singleOrNull()?.let { mapper.toDto(it) }",
                                tableName, tableName
                            )
                            .build()
                    )
                    .addFunction(
                        FunSpec.builder("findAll")
                            .returns(List::class.asClassName().parameterizedBy(ClassName(packageName, dtoName)))
                            .addStatement(
                                "return %L.selectAll().map { mapper.toDto(it) }",
                                tableName
                            )
                            .build()
                    )
                    .addFunction(
                        FunSpec.builder("create")
                            .addParameter("dto", ClassName(packageName, dtoName))
                            .returns(ClassName(packageName, dtoName))
                            .addStatement(
                                "val newRow = %L.insert {",
                                tableName
                            )
                            .apply {
                                properties.forEach { (prop) ->
                                    addStatement("    it[%L.%L] = dto.%L", tableName, prop, prop)
                                }
                            }
                            .addStatement(
                                "}.resultedValues?.singleOrNull() ?: throw IllegalStateException(%P)",
                                "Failed to create $dtoName"
                            )
                            .addStatement("return mapper.toDto(newRow)")
                            .build()
                    )
                    .addFunction(
                        FunSpec.builder("update")
                            .addParameter("dto", ClassName(packageName, dtoName))
                            .returns(ClassName(packageName, dtoName).copy(nullable = true))
                            .addStatement(
                                "val updatedRow = exec(" +
                                        "%P" +
                                        ") { ps ->",
                                "UPDATE \${$tableName.tableName} SET ${properties.joinToString(", ") { "$tableName.${it.first}.name = ?" }} WHERE \${$tableName.id.name} = ? RETURNING \${$tableName.id.name}, ${properties.joinToString(", ") { "$tableName.${it.first}.name" }}"
                            )
                            .apply {
                                properties.forEachIndexed { index, (prop) ->
                                    addStatement("    ps.setObject(%L, dto.%L)", index + 1, prop)
                                }
                                addStatement("    ps.setLong(%L, dto.id)", properties.size + 1)
                            }
                            .addStatement("    ps.executeQuery()")
                            .addStatement("}?.use { rs ->")
                            .addStatement("    if (rs.next()) {")
                            .addStatement("        val row = ResultRow.create(rs, %L.columns)", tableName)
                            .addStatement("        mapper.toDto(row)")
                            .addStatement("    } else {")
                            .addStatement("        null")
                            .addStatement("    }")
                            .addStatement("}")
                            .addStatement("return updatedRow")
                            .build()
                    )
                    .addFunction(
                        FunSpec.builder("delete")
                            .addParameter("id", Long::class)
                            .returns(Boolean::class)
                            .addStatement(
                                "val deletedRows = %L.deleteWhere { %L.id eq id }",
                                tableName, tableName
                            )
                            .addStatement("return deletedRows > 0")
                            .build()
                    )
                    .build()
            )
            .build()

        codeGenerator.createNewFile(
            dependencies = Dependencies(false, dtoClass.containingFile!!),
            packageName = packageName,
            fileName = repositoryName
        ).use { output ->
            output.writer().use { fileSpec.writeTo(it) }
        }
    }
}

class RepositoryProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return RepositoryProcessor(environment.codeGenerator, environment.logger)
    }
}