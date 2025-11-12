/*
 * This is the build configuration for the web application module using Ktor.
 */

plugins {
    application
    id("io.ktor.plugin") version "3.0.3"
}

dependencies {
    implementation(project(":kinfra-api"))
    implementation(project(":action"))
    implementation(project(":kinfra-infra"))

    // Ktor server
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-server-call-logging-jvm")
    implementation("io.ktor:ktor-server-cors-jvm")
    implementation("io.ktor:ktor-server-status-pages-jvm")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.13")

    // Testing
    testImplementation("io.ktor:ktor-server-test-host-jvm")
}

application {
    mainClass = "net.kigawa.kinfra.ApplicationKt"
}

ktor {
    fatJar {
        archiveFileName.set("kinfra-web-${project.version}.jar")
    }
}
