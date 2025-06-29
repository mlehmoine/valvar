// app/build.gradle.kts
plugins {
    id("org.jetbrains.kotlin.jvm") version "2.1.21"
    id("com.google.devtools.ksp") version "2.1.21-2.0.2"
    kotlin("kapt") // Intellij AI tells me mapstruct does not support KSP, so we use kapt instead
}

val exposedVersion = "0.54.0"
val mapstructVersion = "1.6.3"
dependencies {
    implementation("org.jetbrains.exposed:exposed-core:${exposedVersion}")
    implementation("org.jetbrains.exposed:exposed-jdbc:${exposedVersion}")
    implementation("org.jetbrains.exposed:exposed-dao:${exposedVersion}")
    implementation("org.jetbrains.exposed:exposed-jdbc:${exposedVersion}")
    implementation("org.jetbrains.exposed:exposed-java-time:${exposedVersion}")
    implementation("org.postgresql:postgresql:42.7.4")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.21")
    // MapStruct
    implementation("org.mapstruct:mapstruct:${mapstructVersion}")
    kapt("org.mapstruct:mapstruct-processor:${mapstructVersion}") // Change this line from ksp to kapt

    // KSP processor subproject
    implementation(project(":ksp-processor")) // Add this line
    ksp(project(":ksp-processor"))

    implementation("org.slf4j:slf4j-simple:2.0.13")
    implementation("com.zaxxer:HikariCP:6.3.0")

}

repositories {
    mavenCentral()
}

tasks.named("compileKotlin") {
    dependsOn(tasks.named("kspKotlin"))
}

ksp {
    //verbose = true // Enable verbose logging for debugging
    //arg("mapstruct.defaultComponentModel", "default") // Ensure MapStruct uses default component model
}

kapt {
    arguments {
        arg("mapstruct.defaultComponentModel", "default")
    }
}
