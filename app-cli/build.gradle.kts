/*
 * This is the build configuration for the CLI application module.
 */

plugins {
    application
    id("com.github.johnrengelman.shadow")
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
    applicationDefaultJvmArgs = listOf("-Dkinfra.version=${project.version}")
}

tasks.shadowJar {
    archiveBaseName.set("kinfra-cli")
    archiveClassifier.set("")
    archiveVersion.set(project.version.toString())
    manifest {
        attributes["Main-Class"] = "net.kigawa.kinfra.AppKt"
        attributes["Implementation-Version"] = project.version.toString()
    }
}