package com.executor

import kotlinx.serialization.Serializable
import java.util.UUID

enum class ExecutionStatus {
    QUEUED, IN_PROGRESS, FINISHED, FAILED
}

@Serializable
data class ExecuteRequest(
    val command: String,
    val cpuCount: Int = 1,
    val memoryMb: Int = 512
)

@Serializable
data class ExecuteResponse(
    val executionId: String
)

@Serializable
data class StatusResponse(
    val executionId: String,
    val status: String,
    val output: String? = null,
    val error: String? = null
)

data class Execution(
    val id: String = UUID.randomUUID().toString(),
    val request: ExecuteRequest,
    var status: ExecutionStatus = ExecutionStatus.QUEUED,
    var output: String? = null,
    var error: String? = null
)