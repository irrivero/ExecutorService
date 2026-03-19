package com.executor

import kotlin.test.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay

class ExecutionServiceTest {

    private val service = ExecutionService()

    @Test
    fun `submit returns an executionId`() {
        val id = service.submit(ExecuteRequest(command = "echo hello"))
        assertNotNull(id)
        assertTrue(id.isNotEmpty())
    }

    @Test
    fun `getStatus returns null for unknown id`() {
        assertNull(service.getStatus("unknown-id"))
    }

    @Test
    fun `getStatus returns QUEUED immediately after submit`() {
        val id = service.submit(ExecuteRequest(command = "echo hello"))
        val execution = service.getStatus(id)
        assertNotNull(execution)
        assertTrue(execution.status == ExecutionStatus.QUEUED ||
                execution.status == ExecutionStatus.IN_PROGRESS)
    }

    @Test
    fun `execution fails after timeout`() = runBlocking {
        val slowDocker = object : DockerExecutorInterface {
            override suspend fun startContainer(cpuCount: Int, memoryMb: Int) = "fake-container"
            override suspend fun waitForContainer(containerId: String) {}
            override suspend fun executeCommand(containerId: String, command: String): Pair<String, String> {
                delay(10000)
                return Pair("", "")
            }
            override fun stopContainer(containerId: String) {}
        }
        val service = ExecutionService(timeoutMs = 2000, docker = slowDocker)
        val id = service.submit(ExecuteRequest(command = "sleep 10"))
        Thread.sleep(5000)
        val execution = service.getStatus(id)
        assertEquals(ExecutionStatus.FAILED, execution?.status)
        assertEquals("Execution timed out after 2 seconds", execution?.error)
    }
}