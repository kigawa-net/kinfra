/*
 * This is the build configuration for the CLI application module.
 */

plugins {
    application
}

dependencies {
    implementation(project(":model"))
    implementation(project(":action"))
    implementation(project(":infrastructure"))
    implementation("io.insert-koin:koin-core:3.5.6")
    testImplementation("io.insert-koin:koin-test:3.5.6")
}

application {
    mainClass = "net.kigawa.kinfra.AppKt"
}