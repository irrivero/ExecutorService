package com.executor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException

class ExecutionService {
    private val executions = ConcurrentHashMap<String, Execution>()
    private val docker = DockerExecutor()
    private val scope = CoroutineScope(Dispatchers.IO)

    fun submit(request: ExecuteRequest): String {
        val execution = Execution(request = request)
        executions[execution.id] = execution
        scope.launch { run(execution) }
        return execution.id
    }

    fun getStatus(id: String): Execution? = executions[id]

    private suspend fun run(execution: Execution) {
        var containerId: String? = null
        try {
            withTimeout(60_000) {
                containerId = docker.startContainer(
                    execution.request.cpuCount,
                    execution.request.memoryMb
                )
                docker.waitForContainer(containerId!!)
                execution.status = ExecutionStatus.IN_PROGRESS
                val (output, error) = docker.executeCommand(containerId!!, execution.request.command)
                execution.output = output
                execution.error = error.ifEmpty { null }
                execution.status = ExecutionStatus.FINISHED
            }
        } catch (e: TimeoutCancellationException) {
            execution.status = ExecutionStatus.FAILED
            execution.error = "Execution timed out after 60 seconds"
        } catch (e: Exception) {
            execution.status = ExecutionStatus.FAILED
            execution.error = e.message
        } finally {
            containerId?.let { docker.stopContainer(it) }
        }
    }
}