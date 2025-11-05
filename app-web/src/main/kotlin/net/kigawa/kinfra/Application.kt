package net.kigawa.kinfra

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import net.kigawa.kinfra.di.DependencyContainer
import net.kigawa.kinfra.plugins.configureCORS
import net.kigawa.kinfra.plugins.configureMonitoring
import net.kigawa.kinfra.plugins.configureRouting
import net.kigawa.kinfra.plugins.configureSerialization
import net.kigawa.kinfra.plugins.configureStatusPages

lateinit var dependencyContainer: DependencyContainer

fun main() {
    dependencyContainer = DependencyContainer()
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
