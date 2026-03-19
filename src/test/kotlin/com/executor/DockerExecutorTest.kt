package com.executor

import kotlinx.coroutines.runBlocking
import kotlin.test.*
import org.junit.jupiter.api.Tag


@Tag("docker")
class DockerExecutorTest {

    private val docker = DockerExecutor()

    @Test
    fun `start container returns a container id`() = runBlocking {
        val containerId = docker.startContainer(cpuCount = 1, memoryMb = 512)
        assertNotNull(containerId)
        assertTrue(containerId.isNotEmpty())
        docker.stopContainer(containerId)
    }

    @Test
    fun `execute command returns output`() = runBlocking {
        val containerId = docker.startContainer(cpuCount = 1, memoryMb = 512)
        docker.waitForContainer(containerId)
        val (output, error) = docker.executeCommand(containerId, "echo hello")
        assertEquals("hello\n", output)
        assertTrue(error.isEmpty())
        docker.stopContainer(containerId)
    }

    @Test
    fun `stop container removes it`() = runBlocking {
        val containerId = docker.startContainer(cpuCount = 1, memoryMb = 512)
        docker.stopContainer(containerId)
        val process = ProcessBuilder("docker", "inspect", containerId)
            .redirectErrorStream(true)
            .start()
        val exitCode = process.waitFor()
        assertNotEquals(0, exitCode)
    }
}