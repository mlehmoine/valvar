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
            .map { it.simpleName.asString() to it.type.resolve() }
            .toList()

        // Generate repository class
        val fileSpec = FileSpec.builder(packageName, repositoryName)
            .addImport(tablePackage, tableName)
            .addImport("org.jetbrains.exposed.sql", "")
            .addImport("java.sql", "PreparedStatement")
            .addImport("java.sql", "ResultSet")
            .addType(
                TypeSpec.classBuilder(repositoryName)
                    .addFunction(buildFindByIdFun(dtoName, tableName))
                    .addFunction(buildFindAllFun(dtoName, tableName))
                    .addFunction(buildCreateFun(dtoName, tableName, properties))
                    .addFunction(buildUpdateFun(dtoName, tableName, properties))
                    .addFunction(buildDeleteFun(tableName))
                    .addFunction(buildToDtoFun(dtoName, tableName, properties))
                    .build()
            )
            .build()

        // Write to file
        codeGenerator.createNewFile(
            dependencies = Dependencies(false, dtoClass.containingFile!!),
            packageName = packageName,
            fileName = repositoryName
        ).use { output ->
            output.writer().use { fileSpec.writeTo(it) }
        }
    }

    private fun buildFindByIdFun(dtoName: String, tableName: String): FunSpec {
        return FunSpec.builder("findById")
            .addParameter("id", Long::class)
            .returns(ClassName(dtoName.split(".").last(), dtoName).copy(nullable = true))
            .addStatement(
                "return %L.select { %L.id eq id }.singleOrNull()?.toDto()",
                tableName, tableName
            )
            .build()
    }

    private fun buildFindAllFun(dtoName: String, tableName: String): FunSpec {
        return FunSpec.builder("findAll")
            .returns(List::class.asClassName().parameterizedBy(ClassName(dtoName.split(".").last(), dtoName)))
            .addStatement(
                "return %L.selectAll().map { it.toDto() }",
                tableName
            )
            .build()
    }

    private fun buildCreateFun(dtoName: String, tableName: String, properties: List<Pair<String, KSType>>): FunSpec {
        return FunSpec.builder("create")
            .addParameter("dto", ClassName(dtoName.split(".").last(), dtoName))
            .returns(ClassName(dtoName.split(".").last(), dtoName))
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
            .addStatement("return newRow.toDto()")
            .build()
    }

    private fun buildUpdateFun(dtoName: String, tableName: String, properties: List<Pair<String, KSType>>): FunSpec {
        val setClause = properties.joinToString(", ") { "$tableName.${it.first}.name = ?" }
        val returningClause = (listOf("$tableName.id.name") + properties.map { "$tableName.${it.first}.name" }).joinToString(", ")
        return FunSpec.builder("update")
            .addParameter("dto", ClassName(dtoName.split(".").last(), dtoName))
            .returns(ClassName(dtoName.split(".").last(), dtoName).copy(nullable = true))
            .addStatement(
                "val updatedRow = exec(" +
                        "%P" +
                        ") { ps ->",
                "UPDATE \${$tableName.tableName} SET $setClause WHERE \${$tableName.id.name} = ? RETURNING $returningClause"
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
            .addStatement("        %L(", dtoName)
            .addStatement("            id = rs.getLong(%L.id.name),", tableName)
            .apply {
                properties.forEach { (prop, type) ->
                    addStatement("            %L = rs.getObject(%L.%L.name) as %T,", prop, tableName, prop, type.toTypeName())
                }
            }
            .addStatement("        )")
            .addStatement("    } else {")
            .addStatement("        null")
            .addStatement("    }")
            .addStatement("}")
            .addStatement("return updatedRow")
            .build()
    }

    private fun buildDeleteFun(tableName: String): FunSpec {
        return FunSpec.builder("delete")
            .addParameter("id", Long::class)
            .returns(Boolean::class)
            .addStatement(
                "val deletedRows = %L.deleteWhere { %L.id eq id }",
                tableName, tableName
            )
            .addStatement("return deletedRows > 0")
            .build()
    }

    private fun buildToDtoFun(dtoName: String, tableName: String, properties: List<Pair<String, KSType>>): FunSpec {
        return FunSpec.builder("toDto")
            .receiver(ResultRow::class)
            .returns(ClassName(dtoName.split(".").last(), dtoName))
            .addStatement("%L(", dtoName)
            .addStatement("    id = this[%L.id].value,", tableName)
            .apply {
                properties.forEach { (prop) ->
                    addStatement("    %L = this[%L.%L],", prop, tableName, prop)
                }
            }
            .addStatement(")")
            .build()
    }
}

class RepositoryProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return RepositoryProcessor(environment.codeGenerator, environment.logger)
    }
}