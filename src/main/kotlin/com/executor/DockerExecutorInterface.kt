package com.executor

interface DockerExecutorInterface {
    suspend fun startContainer(cpuCount: Int, memoryMb: Int): String
    suspend fun waitForContainer(containerId: String)
    suspend fun executeCommand(containerId: String, command: String): Pair<String, String>
    fun stopContainer(containerId: String)
}