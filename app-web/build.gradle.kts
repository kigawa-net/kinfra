/*
 * This is the build configuration for the web application module using Ktor.
 */

plugins {
    id("app-web")
    application
}

application {
    mainClass = "net.kigawa.kinfra.ApplicationKt"
}

ktor {
    fatJar {
        archiveFileName.set("kinfra-web-${project.version}.jar")
    }
}
