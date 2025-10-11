package net.kigawa.kinfra.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import net.kigawa.kinfra.action.TerraformService
import net.kigawa.kinfra.model.Environment
import org.koin.ktor.ext.inject

@Serializable
data class TerraformRequest(
    val environment: String,
    val command: String
)

@Serializable
data class TerraformResponse(
    val success: Boolean,
    val message: String,
    val output: String? = null,
    val exitCode: Int? = null
)

fun Route.terraformRoutes() {
    val terraformService by inject<TerraformService>()

    route("/terraform") {
        get("/environments") {
            val environments = listOf("dev", "staging", "prod")
            call.respond(environments)
        }

        post("/init") {
            val request = call.receive<TerraformRequest>()
            val env = Environment(request.environment)

            try {
                val result = terraformService.init(env)
                call.respond(
                    TerraformResponse(
                        success = result.exitCode == 0,
                        message = result.message ?: (if (result.exitCode == 0) "Init successful" else "Init failed"),
                        output = result.message,
                        exitCode = result.exitCode
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    TerraformResponse(
                        success = false,
                        message = "Error: ${e.message}"
                    )
                )
            }
        }

        post("/plan") {
            val request = call.receive<TerraformRequest>()
            val env = Environment(request.environment)

            try {
                val result = terraformService.plan(env)
                call.respond(
                    TerraformResponse(
                        success = result.exitCode == 0,
                        message = result.message ?: (if (result.exitCode == 0) "Plan successful" else "Plan failed"),
                        output = result.message,
                        exitCode = result.exitCode
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    TerraformResponse(
                        success = false,
                        message = "Error: ${e.message}"
                    )
                )
            }
        }

        post("/apply") {
            val request = call.receive<TerraformRequest>()
            val env = Environment(request.environment)

            try {
                val result = terraformService.apply(env)
                call.respond(
                    TerraformResponse(
                        success = result.exitCode == 0,
                        message = result.message ?: (if (result.exitCode == 0) "Apply successful" else "Apply failed"),
                        output = result.message,
                        exitCode = result.exitCode
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    TerraformResponse(
                        success = false,
                        message = "Error: ${e.message}"
                    )
                )
            }
        }

        post("/destroy") {
            val request = call.receive<TerraformRequest>()
            val env = Environment(request.environment)

            try {
                val result = terraformService.destroy(env)
                call.respond(
                    TerraformResponse(
                        success = result.exitCode == 0,
                        message = result.message ?: (if (result.exitCode == 0) "Destroy successful" else "Destroy failed"),
                        output = result.message,
                        exitCode = result.exitCode
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    TerraformResponse(
                        success = false,
                        message = "Error: ${e.message}"
                    )
                )
            }
        }

        post("/validate") {
            try {
                val result = terraformService.validate()
                call.respond(
                    TerraformResponse(
                        success = result.exitCode == 0,
                        message = result.message ?: (if (result.exitCode == 0) "Validation successful" else "Validation failed"),
                        output = result.message,
                        exitCode = result.exitCode
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    TerraformResponse(
                        success = false,
                        message = "Error: ${e.message}"
                    )
                )
            }
        }

        post("/format") {
            try {
                val result = terraformService.format()
                call.respond(
                    TerraformResponse(
                        success = result.exitCode == 0,
                        message = result.message ?: (if (result.exitCode == 0) "Format successful" else "Format failed"),
                        output = result.message,
                        exitCode = result.exitCode
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    TerraformResponse(
                        success = false,
                        message = "Error: ${e.message}"
                    )
                )
            }
        }
    }
}