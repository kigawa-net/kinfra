package net.kigawa.kinfra

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import net.kigawa.kinfra.di.DependencyContainer
import net.kigawa.kinfra.plugins.*

lateinit var dependencyContainer: DependencyContainer

fun main() {
    dependencyContainer = DependencyContainer()
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureRouting()
    configureSerialization()
    configureMonitoring()
    configureCORS()
    configureStatusPages()
}