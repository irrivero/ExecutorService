package com.executor

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.testing.*
import kotlin.test.*

class RoutesTest {

    private fun testApp(block: suspend ApplicationTestBuilder.() -> Unit) = testApplication {
        install(ContentNegotiation) { json() }
        routing { }
        application { configureRoutes(ExecutionService()) }
        block()
    }

    @Test
    fun `POST execute returns executionId`() = testApp {
        val response = client.post("/execute") {
            contentType(ContentType.Application.Json)
            setBody("""{"command": "echo hello", "cpuCount": 1, "memoryMb": 512}""")
        }
        assertEquals(HttpStatusCode.Accepted, response.status)
        assertTrue(response.bodyAsText().contains("executionId"))
    }

    @Test
    fun `GET status returns 404 for unknown id`() = testApp {
        val response = client.get("/status/unknown-id")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `GET status returns execution for valid id`() = testApp {
        val post = client.post("/execute") {
            contentType(ContentType.Application.Json)
            setBody("""{"command": "echo hello", "cpuCount": 1, "memoryMb": 512}""")
        }
        val id = post.bodyAsText()
            .substringAfter("\"executionId\":\"")
            .substringBefore("\"")
        val get = client.get("/status/$id")
        assertEquals(HttpStatusCode.OK, get.status)
    }
}