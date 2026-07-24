plugins {
    id("jvm")
    id("io.ktor.plugin")
}

dependencies {
//    implementation(project(":kinfra-api"))
//    implementation(project(":action"))
//    implementation(project(":kinfra-infra"))
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")

    // Ktor server
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-server-call-logging-jvm")
    implementation("io.ktor:ktor-server-cors-jvm")
    implementation("io.ktor:ktor-server-status-pages-jvm")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.6.0")
}
