# Agentic URL Shortener

A production-oriented URL-shortening service and agentic software-engineering
orchestration prototype built with Java 21 and Spring Boot 4.1.0.

The project combines:

1. A reliable URL-shortening API with durable request idempotency.
2. An agentic engineering system that analyzes requirements, generates a
   scenario-specific dependency graph, inspects brownfield repositories, and
   produces traceable engineering artifacts.

## Current status

Implemented through Commit 6:

- Production-style URL-shortening APIs
- PostgreSQL-backed request idempotency
- Versioned workflow execution
- Explicit dependency graphs
- Sequential and parallel task execution
- Entry and exit gates
- Bounded retries
- Requirement normalization and ambiguity detection
- Scenario-aware workflow planning
- Controlled brownfield repository inspection
- Versioned repository-analysis and architecture artifacts
- SHA-256 artifact integrity hashes

Implementation-patch generation, executable validation, governance, recovery,
metrics, scenarios, Docker application packaging, and CI are added in subsequent
commits.

## Implemented capabilities

### URL shortener

- HTTP and HTTPS URL validation
- URL normalization
- Collision-resistant Base62 short-code generation
- Database-enforced short-code uniqueness
- Optional URL expiration
- Active and disabled link states
- URL creation API
- URL details API
- Public redirect API
- PostgreSQL persistence
- Flyway-controlled schema migrations
- RFC 9457 Problem Details responses

### Durable idempotency

- Required `Idempotency-Key` header
- PostgreSQL-backed idempotency reservations
- SHA-256 request fingerprinting
- Same key and payload replay the original resource
- Same key with a different payload returns `409 Conflict`
- In-progress duplicate detection
- Recovery of failed or timed-out reservations
- Original response-status preservation
- `Idempotency-Replayed` response header
- Database locking for concurrent reservation handling

### Agentic orchestration

- Versioned engineering workflows
- Explicit directed acyclic dependency graph
- Missing-dependency validation
- Cycle detection
- Sequential execution
- Parallel task waves
- Join synchronization
- Entry and exit gates
- Context-key gates
- Revision-bound approval gate model
- Thread-safe cross-stage context
- Automatic task execution
- Bounded task retries
- Virtual-thread execution
- In-memory workflow repository

### Requirement understanding and planning

- Deterministic requirement normalization
- Acceptance-criteria generation
- Assumption recording
- Ambiguity detection
- Clarification pause
- Risk classification
- Documentation-only request detection
- Greenfield workflow planning
- Brownfield workflow planning
- High-risk security-review planning
- Automatic workflow creation and execution API
- Workflow inspection API

### Controlled repository reasoning

- Approved repository-root enforcement
- Path-traversal prevention
- Workspace-escape prevention
- Recursive source and test discovery
- Maven, Gradle, and Node build-system detection
- Module detection
- Flyway migration discovery
- Configuration and documentation discovery
- Requirement-based impacted-file identification
- File-count and file-size limits
- Generated repository-analysis evidence

### Engineering artifacts

- Workflow-specific artifact directories
- Revision-specific artifact directories
- Artifact-producing task identity
- SHA-256 artifact hashes
- Artifact creation timestamps
- Artifact sizes
- Append-only file creation
- Repository-analysis Markdown artifact
- Architecture Markdown artifact
- Workflow artifact catalog API

## Architecture

```text
                    Client
                      |
          +-----------+-----------+
          |                       |
          v                       v
   URL Shortener API       Engineering Workflow API
          |                       |
          v                       v
 Idempotent Creation       Requirement Analyzer
          |                       |
          v                       v
    ShortUrlService       Scenario-Aware Planner
          |                       |
          v                       v
      PostgreSQL            Dependency Graph
                                  |
                    +-------------+-------------+
                    |                           |
                    v                           v
          Repository Analysis          Architecture Design
                    |                           |
                    +-------------+-------------+
                                  |
                                  v
                         Versioned Artifacts
```

Workflow execution:

```text
Requirement
    |
    v
Requirement analysis
    |
    +-- ambiguous --> AWAITING_CLARIFICATION
    |
    v
Scenario-aware DAG
    |
    +-- greenfield --> Architecture
    |
    +-- brownfield --> Repository analysis --> Architecture
    |
    +-- docs-only --> Documentation --> Validation
    |
    v
Parallel implementation and test-planning paths
    |
    v
Join synchronization
    |
    v
Validation and release-readiness stages
```

The implementation and validation stages currently produce deterministic
structured outputs. Real patch generation and executable validation are added in
the next commit.

## Technology

- Java 21
- Spring Boot 4.1.0
- Spring MVC
- Spring Data JPA
- Hibernate ORM
- PostgreSQL 17
- Flyway
- Maven
- Lombok
- Jakarta Validation
- JUnit
- AssertJ
- Mockito
- Docker Compose
- Java virtual threads

## Project structure

```text
src/main/java/com/navya/agentic_url_shortener/
|-- agent/
|   |-- architecture/
|   |-- repository/
|   `-- requirement/
|-- artifact/
|-- audit/
|-- common/
|-- config/
|-- governance/
|-- idempotency/
|   |-- domain/
|   |-- dto/
|   |-- exception/
|   |-- repository/
|   `-- service/
|-- orchestration/
|   |-- controller/
|   |-- domain/
|   |-- dto/
|   |-- engine/
|   |-- exception/
|   |-- gate/
|   |-- planner/
|   |-- repository/
|   `-- service/
|-- policy/
|-- tool/
|   `-- repository/
`-- url/
    |-- controller/
    |-- domain/
    |-- dto/
    |-- exception/
    |-- repository/
    `-- service/
```

## Prerequisites

Install:

- JDK 21
- Docker Desktop
- Git

Maven installation is not required because the Maven Wrapper is included.

Verify the tools:

```powershell
java -version
docker version
docker compose version
```

## Local development

### Start PostgreSQL

From the project root:

```powershell
docker compose up -d postgres
docker compose ps
```

Wait until `agentic-url-shortener-postgres` reports `healthy`.

### Run the tests

```powershell
.\mvnw.cmd clean test
```

A successful run ends with:

```text
BUILD SUCCESS
```

### Start the application

Always start it from the project root so relative repository and artifact paths
are resolved consistently:

```powershell
.\mvnw.cmd spring-boot:run
```

The application runs at:

```text
http://localhost:8080
```

### Check health

```powershell
Invoke-RestMethod `
    -Method Get `
    -Uri "http://localhost:8080/actuator/health"
```

Expected:

```json
{
  "status": "UP"
}
```

## URL API

### Create a short URL

```http
POST /api/v1/urls
Content-Type: application/json
Idempotency-Key: request-123
```

```json
{
  "url": "https://example.com/products?id=10",
  "expiresAt": "2027-08-13T12:00:00Z"
}
```

First response:

```http
HTTP/1.1 201 Created
Idempotency-Replayed: false
```

Repeating the same key and payload returns the same resource:

```http
HTTP/1.1 201 Created
Idempotency-Replayed: true
```

Reusing the key with a different payload returns:

```http
HTTP/1.1 409 Conflict
```

### Retrieve URL details

```http
GET /api/v1/urls/{shortCode}
```

### Redirect

```http
GET /{shortCode}
```

An active link returns:

```http
HTTP/1.1 302 Found
Location: https://example.com/products?id=10
```

An expired or disabled link returns `410 Gone`. An unknown code returns
`404 Not Found`.

## Engineering workflow API

### Create a brownfield workflow

The repository path is relative to `AGENT_REPOSITORY_ROOT`. To analyze the
current project, use `"."`.

```powershell
$workflow = Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:8080/api/v1/engineering-workflows" `
    -ContentType "application/json" `
    -Body '{
      "scenarioType": "BROWNFIELD",
      "requirement": "Add redirect analytics to the existing URL API",
      "repositoryPath": "."
    }'
```

Inspect its status:

```powershell
$workflow.status
$workflow.revision
```

Expected:

```text
COMPLETED
1
```

Inspect the generated dependency graph:

```powershell
$workflow.tasks |
    Select-Object name, type, status, dependencyIds, attempt |
    Format-Table -AutoSize
```

### Create a greenfield workflow

```powershell
$greenfield = Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:8080/api/v1/engineering-workflows" `
    -ContentType "application/json" `
    -Body '{
      "scenarioType": "GREENFIELD",
      "requirement": "Create a URL shortening API with expiration",
      "repositoryPath": null
    }'
```

A greenfield plan does not contain `REPOSITORY_ANALYSIS`.

### Create an ambiguous workflow

```powershell
$ambiguous = Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:8080/api/v1/engineering-workflows" `
    -ContentType "application/json" `
    -Body '{
      "scenarioType": "AMBIGUOUS",
      "requirement": "Make shortened links safer and better",
      "repositoryPath": null
    }'
```

Expected:

```text
AWAITING_CLARIFICATION
```

Inspect the clarification questions:

```powershell
$ambiguous.context.ambiguities
```

No architecture or implementation task executes for an ambiguous request.

### Retrieve a workflow

```http
GET /api/v1/engineering-workflows/{workflowId}
```

Example:

```powershell
Invoke-RestMethod `
    -Method Get `
    -Uri "http://localhost:8080/api/v1/engineering-workflows/$($workflow.id)"
```

Workflow state currently remains available for the lifetime of the running
application.

## Engineering artifacts

### List workflow artifacts

```http
GET /api/v1/engineering-workflows/{workflowId}/artifacts
```

```powershell
$artifacts = Invoke-RestMethod `
    -Method Get `
    -Uri "http://localhost:8080/api/v1/engineering-workflows/$($workflow.id)/artifacts"

$artifacts |
    Select-Object type, name, relativePath, sha256, sizeBytes |
    Format-Table -AutoSize
```

Expected artifact types:

```text
REPOSITORY_ANALYSIS
ARCHITECTURE
```

Artifacts are stored under:

```text
agent-workspaces/{workflowId}/revision-{revision}/artifacts/
```

Current files:

```text
repository-analysis.md
architecture.md
```

### Read the repository-analysis artifact

Run these commands from the project root:

```powershell
$repositoryArtifact = $artifacts |
    Where-Object {
        $_.type -eq "REPOSITORY_ANALYSIS"
    } |
    Select-Object -First 1

$repositoryArtifactPath = Join-Path `
    (Get-Location) `
    (Join-Path `
        "agent-workspaces" `
        $repositoryArtifact.relativePath
    )

Test-Path -LiteralPath $repositoryArtifactPath
Get-Content -LiteralPath $repositoryArtifactPath
```

### Read the architecture artifact

```powershell
$architectureArtifact = $artifacts |
    Where-Object {
        $_.type -eq "ARCHITECTURE"
    } |
    Select-Object -First 1

$architectureArtifactPath = Join-Path `
    (Get-Location) `
    (Join-Path `
        "agent-workspaces" `
        $architectureArtifact.relativePath
    )

Test-Path -LiteralPath $architectureArtifactPath
Get-Content -LiteralPath $architectureArtifactPath
```

### Verify an artifact hash

```powershell
(Get-FileHash `
    -Algorithm SHA256 `
    -LiteralPath $repositoryArtifactPath
).Hash.ToLower()
```

The result should equal the artifact API's `sha256` value.

## Repository safety

The repository analyzer may only inspect paths under:

```text
AGENT_REPOSITORY_ROOT
```

Default:

```text
.
```

A path traversal request is rejected:

```powershell
try {
    Invoke-RestMethod `
        -Method Post `
        -Uri "http://localhost:8080/api/v1/engineering-workflows" `
        -ContentType "application/json" `
        -Body '{
          "scenarioType": "BROWNFIELD",
          "requirement": "Inspect the existing URL API",
          "repositoryPath": "../"
        }'
} catch {
    $_.Exception.Response.StatusCode
    $_.ErrorDetails.Message
}
```

Expected:

```text
400 Bad Request
```

## Problem Details

API errors use RFC 9457 Problem Details.

Supported error cases include:

- Invalid URL
- Request validation failure
- Unknown short URL
- Expired or disabled short URL
- Invalid idempotency key
- Idempotency conflict
- Request already in progress
- Invalid workflow graph
- Unknown workflow
- Rejected repository access

## Database inspection

List database tables:

```powershell
docker exec `
    agentic-url-shortener-postgres `
    psql `
    -U postgres `
    -d agentic_url_shortener `
    -c "\dt"
```

Expected application tables:

```text
platform_metadata
short_urls
idempotency_records
flyway_schema_history
```

Inspect URL records:

```powershell
docker exec `
    agentic-url-shortener-postgres `
    psql `
    -U postgres `
    -d agentic_url_shortener `
    -c "SELECT short_code, original_url, status, created_at, expires_at FROM short_urls;"
```

Inspect idempotency records:

```powershell
docker exec `
    agentic-url-shortener-postgres `
    psql `
    -U postgres `
    -d agentic_url_shortener `
    -c "SELECT idempotency_key, status, resource_id, response_status FROM idempotency_records;"
```

## Configuration

| Setting | Environment variable | Default |
|---|---|---|
| Server port | `SERVER_PORT` | `8080` |
| Database URL | `DB_URL` | `jdbc:postgresql://localhost:5432/agentic_url_shortener` |
| Database username | `DB_USERNAME` | `postgres` |
| Database password | `DB_PASSWORD` | `postgres` |
| Short URL base | `SHORT_URL_BASE_URL` | `http://localhost:8080` |
| Short-code length | `SHORT_CODE_LENGTH` | `8` |
| Generation attempts | `SHORT_CODE_GENERATION_ATTEMPTS` | `10` |
| Idempotency retention | `IDEMPOTENCY_RETENTION` | `PT24H` |
| In-progress timeout | `IDEMPOTENCY_IN_PROGRESS_TIMEOUT` | `PT2M` |
| Agent workspace | `AGENT_WORKSPACE_ROOT` | `./agent-workspaces` |
| Approved repository root | `AGENT_REPOSITORY_ROOT` | `.` |
| Maximum inspected files | `AGENT_REPOSITORY_MAX_FILES` | `2000` |
| Maximum inspected file size | `AGENT_REPOSITORY_MAX_FILE_SIZE_BYTES` | `1048576` |
| Agent attempts | `AGENT_MAX_ATTEMPTS` | `2` |
| Command timeout | `AGENT_COMMAND_TIMEOUT_SECONDS` | `120` |
| Model provider | `MODEL_PROVIDER` | `deterministic` |

## Design decisions

### Database-backed idempotency

Idempotency state is stored in PostgreSQL so it survives application restarts and
can support multiple application instances.

### Flyway-owned schemas

Hibernate uses `ddl-auto=validate`. Flyway owns schema creation and evolution.
Hibernate verifies that the mappings match the migrated schema.

### Dynamic workflow planning

The generated plan depends on scenario, ambiguity, risk, and change type. The
system does not apply one fixed task sequence to every requirement.

### Controlled autonomy

Ambiguous requirements stop at `AWAITING_CLARIFICATION`. The system does not
invent missing acceptance criteria and continue into implementation.

### Controlled repository access

All requested repository paths are resolved against an approved root. Normalized
and real paths must remain inside that root. File counts and individual file
sizes are bounded.

### Artifact lineage

Artifacts are separated by workflow and workflow revision. Each reference
records the producing task, hash, timestamp, path, and size. Existing artifacts
cannot be silently overwritten.

### Lombok and JPA

Lombok is used for DTOs and constructor injection. JPA entities do not use
`@Data`, avoiding unsafe generated equality, hashing, and string behavior.

## Testing

The suite currently covers:

- Application context
- URL validation and domain behavior
- Short-code generation
- URL services
- Idempotency fingerprints and state transitions
- Workflow graph validation
- Cycle and missing-dependency rejection
- Parallel execution and join synchronization
- Bounded retry
- Requirement normalization and ambiguity detection
- Scenario-aware planning
- Documentation-only planning
- Repository path enforcement
- Repository analysis
- Artifact creation and hashing

Run:

```powershell
.\mvnw.cmd clean test
```

## Current limitations

- Workflow state is currently stored in memory.
- The artifact catalog is in memory, although artifact files remain on disk.
- Requirement analysis is deterministic rather than model-backed.
- Repository and architecture stages generate real artifacts.
- Implementation and test stages still generate structured placeholders.
- Code-patch generation is not implemented yet.
- Build and test commands are not yet executed by workflow tools.
- Clarification submission and dynamic replanning are not implemented yet.
- Governance and authenticated approval are not implemented yet.
- Audit-grade event persistence and reliability metrics are not implemented yet.
- Redirect analytics are not implemented yet.
- The application is not yet packaged in Docker Compose.
- GitHub Actions CI is not implemented yet.

## Planned next capabilities

- Implementation and test artifacts
- Controlled Maven execution
- Executable validation gates
- Bounded recovery evidence
- Rollback and safe stop
- Clarification-driven replanning
- Policy-backed approvals
- Audit events and reliability metrics
- Greenfield, brownfield, and ambiguous scenario scripts
- Docker application image
- GitHub Actions CI