plugins {
    kotlin("jvm") version "2.1.21"
}

group = "com.segtax"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val exposedVersion = "0.61.0"

dependencies {
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")

    // DAO API, if you want to use Data Access Objects
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")

    // JDBC driver and connection pooling (HikariCP)
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")

    // Support for Java 8+ Date and Time API (LocalDate, etc.)
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")

    // The actual PostgreSQL JDBC driver
    implementation("org.postgresql:postgresql:42.7.3")

    // A logging implementation to see SQL output from Exposed
    // You can use any SLF4J-compatible logger
    implementation("org.slf4j:slf4j-simple:2.0.13")

    // https://mvnrepository.com/artifact/com.zaxxer/HikariCP
    implementation("com.zaxxer:HikariCP:6.3.0")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}