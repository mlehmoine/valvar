plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:2.1.21-2.0.2")
    implementation("org.jetbrains.exposed:exposed-core:0.54.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.21")
    implementation("com.squareup:kotlinpoet:2.2.0")
    //https://mvnrepository.com/artifact/com.squareup/kotlinpoet-ksp
    implementation("com.squareup:kotlinpoet-ksp:2.2.0")
}


repositories {
    mavenCentral()
}