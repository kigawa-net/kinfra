package net.kigawa.kinfra.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

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