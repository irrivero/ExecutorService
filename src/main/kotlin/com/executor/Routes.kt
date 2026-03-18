package com.executor

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRoutes(service: ExecutionService) {
    routing {
        post("/execute") {
            val request = call.receive<ExecuteRequest>()
            val executionId = service.submit(request)
            call.respond(HttpStatusCode.Accepted, ExecuteResponse(executionId))
        }

        get("/status/{id}") {
            val id = call.parameters["id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing id")
            val execution = service.getStatus(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, "Execution not found")
            call.respond(
                StatusResponse(
                    executionId = execution.id,
                    status = execution.status.name,
                    output = execution.output,
                    error = execution.error
                )
            )
        }
    }
}