// ksp-processor/src/main/kotlin/ksp/GenerateRepository.kt
package ksp

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class GenerateRepository(
    val tableClass: KClass<out org.jetbrains.exposed.sql.Table>
)