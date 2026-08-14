# Agentic URL Shortener

A production-oriented URL-shortening service and agentic software-engineering
orchestration prototype built with Java 21 and Spring Boot 4.1.0.

The project combines:

1. A URL-shortening API with durable request idempotency.
2. An agentic engineering system that analyzes requirements, generates dynamic
   dependency graphs, inspects brownfield repositories, creates reviewable
   engineering artifacts, runs validation, and enforces governed release
   approval.

## Current status

Implemented through Commit 8:

- Core URL-shortening APIs
- PostgreSQL-backed idempotent URL creation
- Versioned engineering workflows
- Explicit dependency-graph execution
- Sequential and parallel execution
- Entry, exit, validation, and approval gates
- Bounded task retries
- Requirement normalization and ambiguity detection
- Scenario-aware dynamic planning
- Controlled brownfield repository inspection
- Versioned engineering artifacts with SHA-256 hashes
- Controlled Maven test execution
- Release policies and mandatory approval
- Safe-stop control

The next stages add clarification-driven replanning, audit events, reliability
metrics, executable scenarios, Docker application packaging, and GitHub Actions
CI.

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
- Failed and timed-out reservation recovery
- Original response-status preservation
- `Idempotency-Replayed` response header
- Database locking for concurrent reservation handling

### Requirement understanding

- Deterministic requirement normalization
- Acceptance-criteria generation
- Assumption recording
- Ambiguity detection
- Clarification pause
- Risk classification
- Documentation-only request detection
- High-risk requirement detection

### Dynamic workflow planning

- Greenfield workflow planning
- Brownfield workflow planning
- Documentation-only workflow optimization
- High-risk security-review paths
- Requirement-dependent task graphs
- Automatic workflow execution API
- Workflow inspection API

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
- Human-approval gates
- Thread-safe cross-stage context
- Automatic task execution
- Bounded task retries
- Virtual-thread execution
- In-memory workflow repository

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
- Producing-task identity
- Artifact type and filename
- SHA-256 integrity hashes
- Creation timestamps and sizes
- Append-only file creation
- Workflow artifact catalog API

### Executable validation

- Requirement-specific implementation-plan artifacts
- Requirement-specific test-plan artifacts
- Controlled Maven Wrapper execution
- Fixed allowlisted Maven arguments
- Process timeout enforcement
- Output-size limits
- Exit-code validation
- Captured Maven logs
- Attempt-specific validation evidence
- Validation decision reports
- Failed validation blocks downstream release readiness

### Governance and release control

- Mandatory release-readiness approval
- Approval identity supplied through `X-Actor`
- Request bodies cannot specify or impersonate the approver
- Approval bound to workflow revision
- Approval bound to current artifact hashes
- Validation-success policy
- Validation-evidence policy
- Architecture-evidence policy
- Automatic workflow continuation after approval
- Safe-stop endpoint
- Pending-task cancellation
- Safe-stop actor and reason preservation

## Architecture

```text
                         Client
                           |
              +------------+------------+
              |                         |
              v                         v
       URL Shortener API       Engineering Workflow API
              |                         |
              v                         v
     Idempotent Creation       Requirement Analyzer
              |                         |
              v                         v
       ShortUrlService         Scenario-Aware Planner
              |                         |
              v                         v
          PostgreSQL             Dependency Graph
                                          |
                    +---------------------+---------------------+
                    |                                           |
                    v                                           v
           Repository Analysis                         Architecture Design
                    |                                           |
                    +---------------------+---------------------+
                                          |
                              +-----------+-----------+
                              |                       |
                              v                       v
                    Implementation Plan          Test Plan
                              |                       |
                              +-----------+-----------+
                                          |
                                          v
                                  Maven Validation
                                          |
                                          v
                                    Policy Gates
                                          |
                                          v
                                  Human Approval
                                          |
                                          v
                                 Release Readiness
```

## Workflow states

```text
CREATED
   |
   v
RUNNING
   |
   +-- ambiguous requirement --> AWAITING_CLARIFICATION
   |
   +-- validation failure ----> FAILED
   |
   +-- release gates passed --> AWAITING_APPROVAL
   |                               |
   |                               v
   |                         approval granted
   |                               |
   |                               v
   +--------------------------> COMPLETED
   |
   +-- safe stop -----------> SAFE_STOPPED
```

## Scenario behavior

### Greenfield

A greenfield workflow generates:

```text
Requirement analysis
    -> Architecture
    -> Implementation plan and test plan in parallel
    -> Conditional validation report
    -> Documentation
    -> Approval
    -> Release readiness
```

Greenfield validation is currently conditional because the workflow does not yet
materialize a new source repository.

### Brownfield

A brownfield workflow generates:

```text
Requirement analysis
    -> Repository analysis
    -> Architecture
    -> Implementation plan and test plan in parallel
    -> Controlled Maven validation
    -> Documentation
    -> Policy evaluation
    -> Approval
    -> Release readiness
```

### Ambiguous

An ambiguous workflow stops after requirement analysis:

```text
Requirement analysis
    -> AWAITING_CLARIFICATION
```

No architecture or implementation work starts until clarification is available.

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
|   |-- implementation/
|   |-- repository/
|   |-- requirement/
|   |-- testing/
|   `-- validation/
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
|   |-- build/
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

Verify:

```powershell
java -version
docker version
docker compose version
```

## Local development

### Start PostgreSQL

Run from the project root:

```powershell
docker compose up -d postgres
docker compose ps
```

Wait until `agentic-url-shortener-postgres` reports `healthy`.

### Run tests

```powershell
.\mvnw.cmd clean test
```

A successful run ends with:

```text
BUILD SUCCESS
```

### Start the application

Always start the application from the project root so relative repository and
artifact paths resolve consistently:

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

First request:

```http
HTTP/1.1 201 Created
Idempotency-Replayed: false
```

Equivalent replay:

```http
HTTP/1.1 201 Created
Idempotency-Replayed: true
```

Same key with a different payload:

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

An expired or disabled link returns `410 Gone`. An unknown short code returns
`404 Not Found`.

## Engineering workflow API

### Create a brownfield workflow

The repository path is resolved relative to `AGENT_REPOSITORY_ROOT`. Use `"."`
to analyze this project.

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

The request runs controlled Maven validation and may take longer than an
ordinary API call.

Check the result:

```powershell
$workflow.status
$workflow.revision
```

Expected before approval:

```text
AWAITING_APPROVAL
1
```

Inspect the graph:

```powershell
$workflow.tasks |
    Select-Object name, type, status, dependencyIds, attempt, failureMessage |
    Format-Table -AutoSize
```

The release-readiness task should remain `PENDING` until approval.

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

A greenfield plan does not include `REPOSITORY_ANALYSIS`.

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

Inspect ambiguity questions:

```powershell
$ambiguous.context.ambiguities
```

### Retrieve a workflow

```http
GET /api/v1/engineering-workflows/{workflowId}
```

```powershell
Invoke-RestMethod `
    -Method Get `
    -Uri "http://localhost:8080/api/v1/engineering-workflows/$($workflow.id)"
```

Workflow state is currently available for the lifetime of the application
process because workflow persistence remains in memory.

## Engineering artifacts

### List artifacts

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

### Generated artifact bundle

A successful brownfield workflow currently produces:

```text
agent-workspaces/{workflowId}/revision-{revision}/artifacts/
|-- repository-analysis.md
|-- architecture.md
|-- implementation-plan.md
|-- test-plan.md
|-- maven-test-attempt-1.log
`-- validation-report-attempt-1.md
```

| Artifact | Purpose |
|---|---|
| `repository-analysis.md` | Records project structure, build system, modules, tests, migrations, configuration, and impacted files |
| `architecture.md` | Records design, compatibility concerns, risks, controls, and trade-offs |
| `implementation-plan.md` | Records the requirement-specific implementation sequence and safety constraints |
| `test-plan.md` | Records unit, integration, regression, and validation criteria |
| `maven-test-attempt-1.log` | Preserves controlled Maven test output |
| `validation-report-attempt-1.md` | Records command, exit code, timeout state, duration, log hash, and gate decision |

Retries preserve prior evidence:

```text
maven-test-attempt-2.log
validation-report-attempt-2.md
```

### Read an artifact

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

Get-Content -LiteralPath $repositoryArtifactPath
```

### Verify its hash

```powershell
(Get-FileHash `
    -Algorithm SHA256 `
    -LiteralPath $repositoryArtifactPath
).Hash.ToLower()
```

The result should equal the API's `sha256` value.

## Governance API

### Inspect release policies

```http
GET /api/v1/engineering-workflows/{workflowId}/governance/policies
```

```powershell
$policies = Invoke-RestMethod `
    -Method Get `
    -Uri "http://localhost:8080/api/v1/engineering-workflows/$($workflow.id)/governance/policies"

$policies |
    Format-Table policy, passed, detail -AutoSize
```

A valid brownfield workflow should pass:

```text
POL-VALIDATION-PASSED
POL-VALIDATION-EVIDENCE
POL-ARCHITECTURE-EVIDENCE
```

### Approve release readiness

```http
POST /api/v1/engineering-workflows/{workflowId}/governance/approvals/release-readiness
X-Actor: navya
Content-Type: application/json
```

```powershell
$approved = Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:8080/api/v1/engineering-workflows/$($workflow.id)/governance/approvals/release-readiness" `
    -Headers @{
        "X-Actor" = "navya"
    } `
    -ContentType "application/json" `
    -Body '{
      "reason": "Validation and generated engineering artifacts were reviewed"
    }'
```

Expected:

```powershell
$approved.status
```

```text
COMPLETED
```

Approval records include:

- Workflow ID
- Workflow revision
- Approver
- Reason
- Current artifact hashes
- Approval timestamp

A changed workflow revision does not satisfy an earlier approval gate.

`X-Actor` is a prototype identity boundary. It demonstrates that identity comes
from request context rather than an approver field in the JSON body. Production
deployment requires integration with an authenticated principal.

### Safely stop a workflow

```http
POST /api/v1/engineering-workflows/{workflowId}/governance/safe-stop
X-Actor: navya
Content-Type: application/json
```

```powershell
$stopped = Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:8080/api/v1/engineering-workflows/$($ambiguous.id)/governance/safe-stop" `
    -Headers @{
        "X-Actor" = "navya"
    } `
    -ContentType "application/json" `
    -Body '{
      "reason": "Required clarification is unavailable"
    }'
```

Expected:

```text
SAFE_STOPPED
```

Safe stop prevents pending work from starting, cancels pending or blocked tasks,
and preserves the actor and reason. It does not forcibly interrupt an already
running external Maven process in this prototype.

## Repository safety

Repository access is restricted to:

```text
AGENT_REPOSITORY_ROOT
```

Default:

```text
.
```

A traversal request is rejected:

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

Current error categories include:

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
- Build validation failure
- Governance operation rejection
- Release policy violation

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

Expected:

```text
platform_metadata
short_urls
idempotency_records
flyway_schema_history
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
| Maximum file size | `AGENT_REPOSITORY_MAX_FILE_SIZE_BYTES` | `1048576` |
| Agent attempts | `AGENT_MAX_ATTEMPTS` | `2` |
| Command timeout | `AGENT_COMMAND_TIMEOUT_SECONDS` | `120` |
| Maximum command output | `AGENT_MAX_OUTPUT_CHARACTERS` | `100000` |
| Model provider | `MODEL_PROVIDER` | `deterministic` |

## Design decisions

### Database-backed idempotency

Idempotency records live in PostgreSQL so duplicate handling survives application
restarts and can work across multiple instances.

### Flyway-owned schemas

Flyway owns schema creation and evolution. Hibernate uses `ddl-auto=validate` to
verify mappings without modifying the production schema.

### Dynamic workflow planning

Plans depend on scenario, ambiguity, risk, and request type. The system does not
apply one fixed sequence to every requirement.

### Controlled autonomy

Ambiguous requirements stop before architecture or implementation. Validated
engineering work stops before release readiness until a human approves it.

### Controlled repository access

Repository paths are resolved against an approved root. Normalized and real paths
must remain within that root. Inspected file counts and sizes are bounded.

### Controlled command execution

The build tool runs only the repository's Maven Wrapper with fixed arguments:

```text
--batch-mode --no-transfer-progress test
```

API callers cannot submit commands or command-line arguments.

### Artifact lineage

Artifacts are separated by workflow and revision. Each reference records its
producing task, type, hash, size, path, and creation time. Retry evidence is
preserved instead of overwritten.

### Revision-bound approval

Approval keys contain the release task and workflow revision. Incrementing the
revision prevents old approval evidence from satisfying a new release gate.

### Lombok and JPA

Lombok is used for DTOs and constructor injection. JPA entities avoid `@Data`
because generated equality, hashing, and string behavior are unsafe for mutable
persistence entities.

## Testing

The test suite covers:

- Application startup
- URL validation and domain behavior
- Short-code generation
- URL services
- Idempotency fingerprints and transitions
- Workflow graph validation
- Cycle and missing-dependency rejection
- Parallel execution and synchronization
- Bounded retry
- Requirement analysis and ambiguity detection
- Dynamic scenario planning
- Documentation-only planning
- Repository boundary enforcement
- Repository analysis
- Artifact generation and hashing
- Implementation-plan generation
- Controlled Maven tool rejection
- Release policy enforcement
- Revision-bound approval gates

Run:

```powershell
.\mvnw.cmd clean test
```

## Current limitations

- Workflow state is currently stored in memory.
- The artifact catalog is in memory, although artifact files remain on disk.
- Approval and safe-stop evidence is stored in workflow context.
- `X-Actor` is not authenticated by a security provider.
- Requirement analysis is deterministic rather than model-backed.
- Implementation output is a reviewable plan, not an applied source patch.
- Isolated patch application and rollback are not implemented yet.
- Greenfield workflows do not materialize a source repository.
- Clarification submission and dynamic replanning are not implemented yet.
- Safe stop does not forcibly terminate an already running Maven process.
- Audit-grade event persistence and reliability metrics are not implemented yet.
- Redirect analytics are not implemented yet.
- The application is not yet packaged in Docker Compose.
- GitHub Actions CI is not implemented yet.

## Planned next capabilities

- Clarification-driven workflow replanning
- Approval invalidation evidence after replan
- Persistent audit events
- Reliability and latency metrics
- Runnable greenfield, brownfield, and ambiguous scenarios
- Docker application image
- GitHub Actions CI and coverage gates