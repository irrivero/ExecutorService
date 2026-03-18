# Executor Service

A simple service to execute shell commands on a remote Docker executor, built with Kotlin and Ktor.

## Requirements

- Kotlin 2.2+
- Java 23
- Gradle 8.13
- Docker

## Build
```bash
./gradlew build
```

## Run
```bash
./gradlew run
```

## API

### Execute a command
```
POST /execute
{
  "command": "echo hello",
  "cpuCount": 1,
  "memoryMb": 512
}
```

### Get execution status
```
GET /status/{executionId}
```

## Flow
```mermaid
flowchart TD
    A[User sends POST /execute] --> B[Service creates executionId]
    B --> C[Returns executionId with status QUEUED]
    C --> D[Background: start Docker container]
    D --> E{Container ready?}
    E -->|No| F[Wait and retry]
    F --> E
    E -->|Yes| G[Execute command in container]
    G --> H[Status: IN_PROGRESS]
    H --> I[Command finishes]
    I --> J[Collect output]
    J --> K[Stop container]
    K --> L[Status: FINISHED]
    L --> M[User polls GET /status/:id]
```

## TODO

### Setup
- [x] Project structure with Kotlin + Ktor + Gradle
- [ ] Docker integration

### API
- [ ] POST /execute endpoint
- [ ] GET /status/:id endpoint

### Execution
- [ ] Start Docker container
- [ ] Wait for container to be ready
- [ ] Execute command in container
- [ ] Collect output
- [ ] Update status
- [ ] Stop container

### Error handling
- [ ] Handle container start failure
- [ ] Handle command execution failure
- [ ] Handle timeout

### Tests
- [ ] Unit tests for ExecutionService
- [ ] Integration tests for API endpoints