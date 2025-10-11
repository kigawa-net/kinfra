package net.kigawa.kinfra

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import net.kigawa.kinfra.di.webModule
import net.kigawa.kinfra.plugins.*
import org.koin.ktor.plugin.Koin

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(Koin) {
        modules(webModule)
    }
    configureRouting()
    configureSerialization()
    configureMonitoring()
    configureCORS()
    configureStatusPages()
}