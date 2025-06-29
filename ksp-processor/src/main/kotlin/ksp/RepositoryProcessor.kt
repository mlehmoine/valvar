// ksp-processor/src/main/kotlin/ksp/RepositoryProcessor.kt
package ksp

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toTypeName

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

        // Get the table class from the annotation
        val tableClassValue = annotation.arguments
            .find { it.name?.asString() == "tableClass" }
            ?.value as? KSType
            ?: return logger.error("No tableClass specified for $dtoName")

        val tableClass = tableClassValue.declaration as? KSClassDeclaration
            ?: return logger.error("tableClass is not a valid class declaration")

        val tableName = tableClass.simpleName.asString()
        val tablePackage = tableClass.packageName.asString()

        // Get DTO properties (excluding id)
        val properties = dtoClass.getAllProperties()
            .filter { it.simpleName.asString() != "id" }
            .map { it.simpleName.asString() to it.type.resolve().toTypeName() }
            .toList()

        val fileSpec = FileSpec.builder(packageName, repositoryName)
            .addImport(tablePackage, tableName)
            .addImport("org.jetbrains.exposed.sql", "selectAll", "insert", "updateReturning", "deleteWhere")
            .addImport( "org.jetbrains.exposed.sql.SqlExpressionBuilder", "eq")
            .addType(
                TypeSpec.classBuilder(repositoryName)
                    .addModifiers(KModifier.OPEN)
                    .addProperty(
                        PropertySpec.builder("table", ClassName(tablePackage, tableName))
                            .initializer("%L", tableName)
                            .addModifiers(KModifier.PROTECTED)
                            .build()
                    )
                    .addFunction(
                        FunSpec.builder("toDto")
                            .addModifiers(KModifier.PROTECTED)
                            .addParameter("row", ClassName("org.jetbrains.exposed.sql", "ResultRow"))
                            .returns(ClassName(packageName, dtoName))
                            .addCode(
                                buildString {
                                    append("return $dtoName(\n")
                                    append("    id = row[$tableName.id]")
                                    properties.forEach { (propName, _) ->
                                        append(",\n    $propName = row[$tableName.$propName]")
                                    }
                                    append("\n)\n")
                                }
                            )
                            .build()
                    )
                    .addFunction(
                        FunSpec.builder("findById")
                            .addParameter("id", Long::class)
                            .returns(ClassName(packageName, dtoName).copy(nullable = true))
                            .addStatement(
                                "return %L.selectAll().where { %L.id eq id }.singleOrNull()?.let { toDto(it) }",
                                tableName, tableName
                            )
                            .build()
                    )
                    .addFunction(
                        FunSpec.builder("findAll")
                            .returns(List::class.asClassName().parameterizedBy(ClassName(packageName, dtoName)))
                            .addStatement(
                                "return %L.selectAll().map { toDto(it) }",
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
                            .addStatement("return toDto(newRow)")
                            .build()
                    )
                    .addFunction(
                        FunSpec.builder("update")
                            .addModifiers(KModifier.PUBLIC)
                            .addParameter("dto", ClassName(packageName, dtoName))
                            .returns(ClassName(packageName, dtoName).copy(nullable = true))
                            .addStatement(
                                "val resultRow = %L.updateReturning( where = { %L.id eq dto.id!! }) {",
                                tableName, tableName
                            )
                            .apply {
                                properties.forEach { (prop) ->
                                    addStatement("    it[%L.%L] = dto.%L", tableName, prop, prop)
                                }
                            }
                            .addStatement("}.singleOrNull()")
                            .addStatement("return resultRow?.let { toDto(it) }")
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