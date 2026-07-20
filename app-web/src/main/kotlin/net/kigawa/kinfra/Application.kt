package net.kigawa.kinfra

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import net.kigawa.kinfra.plugins.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        module()
    }
        .start(wait = true)
}

fun Application.module() {
    configureRouting()
    configureSerialization()
    configureMonitoring()
    configureCORS()
    configureStatusPages()
}
