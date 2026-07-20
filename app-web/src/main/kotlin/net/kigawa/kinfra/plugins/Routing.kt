package net.kigawa.kinfra.plugins

import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Kinfra Web API - Terraform infrastructure management")
        }

        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }
    }
}
