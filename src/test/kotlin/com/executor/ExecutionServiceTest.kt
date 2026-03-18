package com.executor

import kotlin.test.*

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
}