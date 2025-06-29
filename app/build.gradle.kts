// app/build.gradle.kts
plugins {
    id("org.jetbrains.kotlin.jvm")
    id("com.google.devtools.ksp")
}

val exposedVersion = "0.54.0"

dependencies {
    implementation("org.jetbrains.exposed:exposed-core:${exposedVersion}")
    implementation("org.jetbrains.exposed:exposed-jdbc:${exposedVersion}")
    implementation("org.jetbrains.exposed:exposed-dao:${exposedVersion}")
    implementation("org.jetbrains.exposed:exposed-jdbc:${exposedVersion}")
    implementation("org.jetbrains.exposed:exposed-java-time:${exposedVersion}")
    implementation("org.postgresql:postgresql:42.7.4")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.21")
    // MapStruct
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    ksp("org.mapstruct:mapstruct-processor:1.5.5.Final")
    // KSP processor subproject
    ksp(project(":ksp-processor"))

    implementation("org.slf4j:slf4j-simple:2.0.13")
    implementation("com.zaxxer:HikariCP:6.3.0")

}

repositories {
    mavenCentral()
}