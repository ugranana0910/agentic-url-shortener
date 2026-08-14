# Agentic URL Shortener

A production-oriented URL-shortening service and controlled agentic
software-engineering orchestration platform built with Java 21 and Spring Boot
4.1.0.

The project demonstrates two connected capabilities:

1. A production-style URL-shortening API with durable idempotency.
2. An agentic engineering system that transforms requirements into planned,
   validated, governed, observable, and reviewable engineering outcomes.

## Status

Implemented through Commit 10:

- Production-oriented URL-shortening APIs
- PostgreSQL-backed durable idempotency
- Requirement analysis and ambiguity detection
- Scenario-aware dynamic workflow planning
- Directed acyclic task graphs
- Sequential and parallel task execution
- Controlled brownfield repository analysis
- Versioned engineering artifacts
- Executable Maven validation
- Policy-backed release gates
- Human approval and safe-stop controls
- Structured workflow audit events
- Micrometer and Prometheus metrics
- JaCoCo coverage verification
- Non-root Docker application image
- Docker Compose environment
- GitHub Actions CI pipeline
- Architecture and operational documentation

## Core capabilities

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
- Flyway-controlled migrations
- RFC 9457 Problem Details responses

### Durable idempotency

`POST /api/v1/urls` requires an `Idempotency-Key` header.

The implementation provides:

- PostgreSQL-backed idempotency reservations
- SHA-256 request fingerprints
- Equivalent-request replay
- Payload-conflict detection
- In-progress duplicate detection
- Failed-reservation recovery
- Timed-out reservation recovery
- Original response-status preservation
- Database locking for concurrent requests
- `Idempotency-Replayed` response header

The expected behavior is:

| Situation | Result |
|---|---|
| First key and payload | Creates the URL and returns `201 Created` |
| Same key and same payload | Replays the original `201` response |
| Same key and different payload | Returns `409 Conflict` |
| Equivalent concurrent request | Serialized through the database reservation |
| Failed or expired reservation | Can be retried safely |

### Requirement understanding

- Requirement normalization
- Acceptance-criteria generation
- Assumption recording
- Ambiguity detection
- Clarification questions
- Risk classification
- Documentation-only detection
- High-risk requirement detection

### Dynamic planning

The generated task graph depends on:

- Scenario type
- Requirement ambiguity
- Requirement risk
- Repository availability
- Documentation-only intent
- Validation requirements
- Human approval requirements

The application does not use one fixed workflow for every request.

### Agentic orchestration

- Versioned engineering workflows
- Explicit directed acyclic graphs
- Missing-dependency validation
- Cycle detection
- Sequential task execution
- Parallel task waves
- Join synchronization
- Entry gates
- Exit gates
- Context-evidence gates
- Human-approval gates
- Bounded retries
- Virtual-thread execution
- Thread-safe workflow context

### Controlled repository reasoning

- Approved repository-root enforcement
- Path-traversal prevention
- Workspace-escape prevention
- File-count limits
- File-size limits
- Recursive source and test discovery
- Maven, Gradle, and Node build detection
- Module discovery
- Flyway migration discovery
- Configuration discovery
- Documentation discovery
- Requirement-based impacted-file identification

### Engineering artifacts

Generated artifacts are isolated by workflow and revision:

```text
agent-workspaces/{workflowId}/revision-{revision}/artifacts/
|-- repository-analysis.md
|-- architecture.md
|-- implementation-plan.md
|-- test-plan.md
|-- maven-test-attempt-1.log
`-- validation-report-attempt-1.md
```

Each artifact reference records:

- Workflow ID
- Workflow revision
- Producing task
- Artifact type
- Filename
- Relative path
- Creation timestamp
- Size
- SHA-256 hash

Retry evidence is preserved rather than overwritten:

```text
maven-test-attempt-2.log
validation-report-attempt-2.md
```

### Executable validation

Brownfield validation executes the repository Maven Wrapper using fixed,
application-controlled arguments:

```text
--batch-mode --no-transfer-progress test
```

Validation includes:

- Command allowlisting
- Fixed Maven arguments
- Process timeout
- Output-size limits
- Exit-code verification
- Captured build logs
- Attempt-specific validation reports
- Validation evidence hashes
- Failure propagation to release gates

API clients cannot provide arbitrary commands or command-line arguments.

### Governance

- Mandatory release-readiness approval
- Policy evaluation before approval
- Validation-success policy
- Validation-evidence policy
- Architecture-evidence policy
- Actor identity supplied through `X-Actor`
- Approval bound to workflow revision
- Approval bound to artifact hashes
- Safe-stop support
- Pending-task cancellation
- Actor and reason preservation

### Auditability

Structured audit events include:

```text
WORKFLOW_CREATED
PLAN_GENERATED
WORKFLOW_STARTED
TASK_STARTED
TASK_SUCCEEDED
TASK_FAILED
TASK_RETRIED
CLARIFICATION_REQUIRED
POLICY_EVALUATED
APPROVAL_GRANTED
SAFE_STOPPED
WORKFLOW_COMPLETED
WORKFLOW_FAILED
```

Each event records:

- Unique event ID
- Workflow ID
- Workflow revision
- Optional task ID
- Event type
- Actor
- Detail
- Timestamp

### Observability

Micrometer and Spring Boot Actuator expose:

- Workflow-started counters
- Workflow-completed counters
- Workflow-failed counters
- Task-success counters
- Task-failure counters
- Retry counters
- Clarification counters
- Approval counters
- Safe-stop counters
- Workflow-duration timers
- Prometheus-formatted metrics

Workflow IDs are deliberately excluded from metric tags to prevent
high-cardinality telemetry.

## Architecture

A detailed architecture description is available in
[`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).

```text
                         Client
                           |
              +------------+------------+
              |                         |
              v                         v
       URL Shortener API       Engineering Workflow API
              |                         |
              v                         v
       Idempotency Layer       Requirement Analyzer
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
                                          |
                    +---------------------+---------------------+
                    |                                           |
                    v                                           v
              Audit Journal                              Micrometer Metrics
                    |                                           |
                    v                                           v
          Workflow Audit API                         Actuator/Prometheus
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

```text
Requirement analysis
    -> Architecture
    -> Implementation plan and test plan
    -> Conditional validation
    -> Documentation
    -> Approval
    -> Release readiness
```

Greenfield validation is conditional because the current prototype does not
materialize a new source repository.

### Brownfield

```text
Requirement analysis
    -> Repository analysis
    -> Architecture
    -> Implementation plan and test plan
    -> Controlled Maven validation
    -> Documentation
    -> Policy evaluation
    -> Approval
    -> Release readiness
```

### Ambiguous

```text
Requirement analysis
    -> AWAITING_CLARIFICATION
```

Architecture, implementation, and validation tasks do not start when mandatory
clarification is required.

## Technology

- Java 21
- Spring Boot 4.1.0
- Spring MVC
- Spring Data JPA
- Hibernate ORM
- PostgreSQL 17
- Flyway
- Maven Wrapper
- Lombok
- Jakarta Validation
- JUnit
- AssertJ
- Mockito
- JaCoCo
- Micrometer
- Prometheus
- Spring Boot Actuator
- Docker
- Docker Compose
- GitHub Actions
- Java virtual threads

## Project structure

```text
.
|-- .github/
|   `-- workflows/
|       `-- ci.yml
|-- docs/
|   `-- ARCHITECTURE.md
|-- src/
|   |-- main/
|   |   |-- java/com/navya/agentic_url_shortener/
|   |   |   |-- agent/
|   |   |   |-- artifact/
|   |   |   |-- audit/
|   |   |   |-- common/
|   |   |   |-- config/
|   |   |   |-- governance/
|   |   |   |-- idempotency/
|   |   |   |-- orchestration/
|   |   |   |-- policy/
|   |   |   |-- tool/
|   |   |   `-- url/
|   |   `-- resources/
|   |       |-- db/migration/
|   |       `-- application.yaml
|   `-- test/
|       |-- java/
|       `-- resources/
|-- .dockerignore
|-- .env.example
|-- .gitignore
|-- Dockerfile
|-- docker-compose.yml
|-- mvnw
|-- mvnw.cmd
|-- pom.xml
`-- README.md
```

## Prerequisites

Install:

- JDK 21
- Docker Desktop
- Git

A separate Maven installation is not required because the Maven Wrapper is
included.

Verify:

```powershell
java -version
docker version
docker compose version
git --version
```

## Local development

### Start PostgreSQL only

```powershell
docker compose up -d postgres
docker compose ps
```

Wait until `agentic-url-shortener-postgres` reports `healthy`.

### Run tests

```powershell
.\mvnw.cmd clean test
```

### Run tests with coverage verification

```powershell
.\mvnw.cmd clean verify
```

A successful build ends with:

```text
BUILD SUCCESS
```

The JaCoCo report is generated at:

```text
target/site/jacoco/index.html
```

Open it on Windows:

```powershell
Invoke-Item ".\target\site\jacoco\index.html"
```

### Start the application locally

```powershell
.\mvnw.cmd spring-boot:run
```

The application starts at:

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

## Docker deployment

### Validate the Compose configuration

```powershell
docker compose config
```

### Build the application image

```powershell
docker compose build
```

### Start PostgreSQL and the application

Make sure no locally running application is already using port `8080`.

```powershell
docker compose up -d
docker compose ps
```

Expected services:

```text
agentic-url-shortener-postgres
agentic-url-shortener-app
```

Wait until both containers report `healthy`.

### View logs

```powershell
docker compose logs application
docker compose logs postgres
```

Follow application logs:

```powershell
docker compose logs -f application
```

### Verify the containerized application

```powershell
Invoke-RestMethod `
    -Method Get `
    -Uri "http://localhost:8080/actuator/health"
```

### Stop containers

```powershell
docker compose down
```

This preserves named volumes.

To remove containers and named volumes:

```powershell
docker compose down --volumes
```

The `--volumes` operation deletes local PostgreSQL and workspace data and should
only be used when that data is no longer needed.

## URL API

### Create a short URL

```powershell
$response = Invoke-WebRequest `
    -Method Post `
    -Uri "http://localhost:8080/api/v1/urls" `
    -Headers @{
        "Idempotency-Key" = "request-123"
    } `
    -ContentType "application/json" `
    -Body '{
      "url": "https://example.com/products?id=10",
      "expiresAt": "2027-08-13T12:00:00Z"
    }'

$response.StatusCode
$response.Headers["Idempotency-Replayed"]
$response.Content
```

Expected first response:

```text
Status: 201
Idempotency-Replayed: false
```

### Replay the request

Run the same request with the same key and payload.

Expected:

```text
Status: 201
Idempotency-Replayed: true
```

The application returns the original resource representation.

### Test an idempotency conflict

```powershell
try {
    Invoke-RestMethod `
        -Method Post `
        -Uri "http://localhost:8080/api/v1/urls" `
        -Headers @{
            "Idempotency-Key" = "request-123"
        } `
        -ContentType "application/json" `
        -Body '{
          "url": "https://different.example.com",
          "expiresAt": null
        }'
} catch {
    $_.Exception.Response.StatusCode
    $_.ErrorDetails.Message
}
```

Expected:

```text
409 Conflict
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

The repository path is resolved relative to `AGENT_REPOSITORY_ROOT`. Use `"."`
to analyze the configured repository root.

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

Controlled Maven validation may make this request take longer than an ordinary
API request.

Inspect the result:

```powershell
$workflow.id
$workflow.status
$workflow.revision
```

Expected before approval:

```text
AWAITING_APPROVAL
1
```

Inspect the generated task graph:

```powershell
$workflow.tasks |
    Select-Object name, type, status, dependencyIds, attempt, failureMessage |
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

A greenfield plan does not contain a `REPOSITORY_ANALYSIS` task.

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

Expected status:

```text
AWAITING_CLARIFICATION
```

Inspect clarification questions:

```powershell
$ambiguous.context.ambiguities
```

### Retrieve a workflow

```powershell
Invoke-RestMethod `
    -Method Get `
    -Uri "http://localhost:8080/api/v1/engineering-workflows/$($workflow.id)"
```

Workflow state is retained for the lifetime of the current application process.

## Engineering artifact API

### List artifacts

```powershell
$artifacts = Invoke-RestMethod `
    -Method Get `
    -Uri "http://localhost:8080/api/v1/engineering-workflows/$($workflow.id)/artifacts"

$artifacts |
    Select-Object type, name, relativePath, sha256, sizeBytes |
    Format-Table -AutoSize
```

### Read a generated artifact

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

When the application runs inside Docker, artifacts are stored inside the named
`agentic_url_shortener_workspaces` volume.

### Verify an artifact hash

```powershell
(Get-FileHash `
    -Algorithm SHA256 `
    -LiteralPath $repositoryArtifactPath
).Hash.ToLower()
```

The result should equal the artifact API response's `sha256` value.

## Governance API

### Inspect release policies

```powershell
$policies = Invoke-RestMethod `
    -Method Get `
    -Uri "http://localhost:8080/api/v1/engineering-workflows/$($workflow.id)/governance/policies"

$policies |
    Format-Table policy, passed, detail -AutoSize
```

Expected brownfield policies include:

```text
POL-VALIDATION-PASSED
POL-VALIDATION-EVIDENCE
POL-ARCHITECTURE-EVIDENCE
```

### Approve release readiness

```powershell
$approved = Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:8080/api/v1/engineering-workflows/$($workflow.id)/governance/approvals/release-readiness" `
    -Headers @{
        "X-Actor" = "navya"
    } `
    -ContentType "application/json" `
    -Body '{
      "reason": "Validation and engineering artifacts were reviewed"
    }'

$approved.status
```

Expected:

```text
COMPLETED
```

Approval evidence contains:

- Workflow ID
- Workflow revision
- Approver
- Reason
- Artifact hashes
- Approval timestamp

A changed workflow revision cannot reuse an earlier approval.

`X-Actor` is a prototype identity boundary. A production deployment should
replace it with an authenticated principal supplied by the security context.

### Safely stop a workflow

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

$stopped.status
```

Expected:

```text
SAFE_STOPPED
```

Safe stop prevents pending work from starting and preserves the actor and
reason.

## Audit API

Retrieve workflow audit events:

```powershell
$auditEvents = Invoke-RestMethod `
    -Method Get `
    -Uri "http://localhost:8080/api/v1/engineering-workflows/$($workflow.id)/audit-events"

$auditEvents |
    Select-Object type, actor, detail, occurredAt |
    Format-Table -AutoSize
```

For an ambiguous workflow, expected events include:

```text
WORKFLOW_CREATED
PLAN_GENERATED
WORKFLOW_STARTED
TASK_STARTED
TASK_SUCCEEDED
CLARIFICATION_REQUIRED
```

An unknown or stale workflow ID returns `404 Not Found`.

Because workflows and audit events are currently in memory, create a new
workflow after every application restart before testing this endpoint.

## Metrics

### List Actuator metrics

```powershell
Invoke-RestMethod `
    -Method Get `
    -Uri "http://localhost:8080/actuator/metrics"
```

### Workflow metrics

```powershell
Invoke-RestMethod `
    -Method Get `
    -Uri "http://localhost:8080/actuator/metrics/agentic.workflows"
```

### Task metrics

```powershell
Invoke-RestMethod `
    -Method Get `
    -Uri "http://localhost:8080/actuator/metrics/agentic.tasks"
```

### Retry metrics

```powershell
Invoke-RestMethod `
    -Method Get `
    -Uri "http://localhost:8080/actuator/metrics/agentic.retries"
```

### Workflow-duration metrics

```powershell
Invoke-RestMethod `
    -Method Get `
    -Uri "http://localhost:8080/actuator/metrics/agentic.workflow.duration"
```

Additional metric names include:

```text
agentic.clarifications
agentic.approvals
agentic.safe.stops
```

### Prometheus output

```powershell
Invoke-WebRequest `
    -Uri "http://localhost:8080/actuator/prometheus" |
    Select-Object -ExpandProperty Content |
    Select-String "agentic_"
```

## Repository safety

Repository access is restricted to:

```text
AGENT_REPOSITORY_ROOT
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

Error categories include:

- Request validation failure
- Invalid URL
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
| Reservation timeout | `IDEMPOTENCY_IN_PROGRESS_TIMEOUT` | `PT2M` |
| Agent workspace | `AGENT_WORKSPACE_ROOT` | `./agent-workspaces` |
| Approved repository root | `AGENT_REPOSITORY_ROOT` | `.` |
| Maximum inspected files | `AGENT_REPOSITORY_MAX_FILES` | `2000` |
| Maximum inspected file size | `AGENT_REPOSITORY_MAX_FILE_SIZE_BYTES` | `1048576` |
| Maximum task attempts | `AGENT_MAX_ATTEMPTS` | `2` |
| Command timeout | `AGENT_COMMAND_TIMEOUT_SECONDS` | `120` |
| Maximum command output | `AGENT_MAX_OUTPUT_CHARACTERS` | `100000` |
| Model provider | `MODEL_PROVIDER` | `deterministic` |

## Continuous integration

The GitHub Actions workflow is defined in:

```text
.github/workflows/ci.yml
```

The pipeline runs on pushes and pull requests and performs:

1. Repository checkout
2. Java 21 setup
3. Maven dependency caching
4. Unit and application tests
5. JaCoCo report generation
6. Coverage-gate verification
7. Test-report upload on failure
8. JaCoCo artifact upload
9. Docker Compose validation
10. Docker image build validation

Run the equivalent Maven validation locally:

```powershell
.\mvnw.cmd clean verify
```

Run the equivalent Docker validations locally:

```powershell
docker compose config
docker build --tag agentic-url-shortener:local .
```

## Testing

The test suite covers:

- Application startup
- URL validation
- URL domain behavior
- Short-code generation
- URL services
- Idempotency fingerprints
- Idempotency state transitions
- Workflow graph validation
- Cycle rejection
- Missing-dependency rejection
- Parallel execution
- Join synchronization
- Bounded retry
- Requirement analysis
- Ambiguity detection
- Dynamic scenario planning
- Documentation-only planning
- Repository boundary enforcement
- Repository analysis
- Artifact generation
- Artifact hashing
- Implementation-plan generation
- Controlled Maven tool rejection
- Release policy enforcement
- Revision-bound approval
- Audit-event ordering
- Workflow-engine audit integration
- Workflow metrics
- Task metrics
- Retry metrics
- Approval and safe-stop metrics

Run all tests and the coverage gate:

```powershell
.\mvnw.cmd clean verify
```

## Design decisions

### Database-backed idempotency

Idempotency records are stored in PostgreSQL so duplicate handling survives
application restarts and can operate across multiple instances.

### Flyway-owned schemas

Flyway owns schema creation and evolution. Hibernate uses
`ddl-auto=validate` to verify mappings without changing the schema.

### Dynamic planning

Workflow graphs depend on scenario, ambiguity, risk, repository availability,
and request intent.

### Controlled autonomy

Ambiguous requirements pause before implementation. Validated engineering work
pauses before release readiness until a human grants approval.

### Controlled repository access

Normalized and real repository paths must remain within the approved root. File
counts and file sizes are bounded.

### Controlled command execution

Only the repository Maven Wrapper can be invoked, using fixed arguments.
API callers cannot submit commands.

### Artifact lineage

Artifacts are separated by workflow and revision and include their producing
task, type, hash, size, path, and timestamp.

### Revision-bound approval

An approval is bound to the workflow revision and current artifact hashes.
Changing the revision invalidates earlier approval evidence.

### Audit events and metrics

Audit events provide workflow-specific traceability. Metrics provide aggregate
operational visibility.

Workflow IDs are not metric tags because unbounded identifiers would create
high-cardinality telemetry.

### Container runtime

The application container runs as a non-root user. The runtime includes Maven
and the project repository because controlled brownfield validation requires a
build tool and reviewable repository input.

### Coverage gate

JaCoCo generates a coverage report during Maven `verify` and fails the build
when the configured minimum line-coverage ratio is not satisfied.

## Current limitations

- Workflow state is stored in memory.
- Artifact references are stored in memory, although generated files remain on
  disk.
- Audit events are stored in memory.
- Workflow and audit state is lost after an application restart.
- Metrics require external collection for long-term retention.
- `X-Actor` is not authenticated by a security provider.
- Requirement analysis uses a deterministic provider rather than an external
  language model.
- Implementation output is a reviewable implementation plan rather than an
  automatically applied patch.
- Isolated patch application and rollback are not implemented.
- Greenfield workflows do not materialize a new repository.
- Clarification submission and dynamic replanning are not implemented.
- Safe stop does not forcibly terminate an already running external process.
- Redirect analytics are used as a demonstration requirement but are not part
  of the core URL API implementation.

## Production evolution

The current design provides clear extension points for:

- PostgreSQL-backed workflow persistence
- Persistent append-only audit storage
- Authenticated actor identity
- Model-backed requirement analysis
- Clarification submission and replanning
- Isolated workspace cloning
- Patch generation and controlled patch application
- Rollback execution
- Distributed metrics collection
- OpenTelemetry tracing
- External artifact storage
- Kubernetes deployment

## Final verification

Before submission, run:

```powershell
.\mvnw.cmd clean verify
docker compose config
docker compose build
docker compose up -d
docker compose ps
```

Verify health:

```powershell
Invoke-RestMethod `
    -Method Get `
    -Uri "http://localhost:8080/actuator/health"
```

Verify Prometheus metrics:

```powershell
Invoke-WebRequest `
    -Uri "http://localhost:8080/actuator/prometheus" |
    Select-Object -ExpandProperty Content |
    Select-String "agentic_"
```

Stop the environment:

```powershell
docker compose down
```