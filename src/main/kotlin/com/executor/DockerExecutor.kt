package com.executor

import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers

class DockerExecutor {

    suspend fun startContainer(cpuCount: Int, memoryMb: Int): String {
        val process = ProcessBuilder(
            "docker", "run", "-d",
            "--cpus", cpuCount.toString(),
            "--memory", "${memoryMb}m",
            "alpine",
            "tail", "-f", "/dev/null"
        )
            .redirectErrorStream(true)
            .start()

        val containerId = process.inputStream.bufferedReader().readLine()?.trim()
            ?: throw RuntimeException("Failed to start container")

        process.waitFor()
        return containerId
    }

    suspend fun waitForContainer(containerId: String) {
        repeat(10) {
            val process = ProcessBuilder("docker", "inspect", "-f", "{{.State.Running}}", containerId)
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().readLine()?.trim()
            process.waitFor()
            if (output == "true") return
            delay(500)
        }
        throw RuntimeException("Container $containerId did not start in time")
    }

    suspend fun executeCommand(containerId: String, command: String): Pair<String, String> {
        val process = ProcessBuilder(
            "docker", "exec", containerId,
            "sh", "-c", command
        )
            .redirectErrorStream(false)
            .start()

        return try {
            withContext(Dispatchers.IO) {
                val output = process.inputStream.bufferedReader().readText()
                val error = process.errorStream.bufferedReader().readText()
                process.waitFor()
                Pair(output, error)
            }
        } catch (e: CancellationException) {
            process.destroy()
            throw e
        }
    }

    fun stopContainer(containerId: String) {
        ProcessBuilder("docker", "stop", containerId)
            .start()
            .waitFor()
        ProcessBuilder("docker", "rm", containerId)
            .start()
            .waitFor()
    }
}