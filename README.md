# Agentic URL Shortener

A production-oriented URL-shortening service and agentic software-engineering
orchestration prototype.

The project demonstrates two connected capabilities:

1. A reliable URL-shortening application with durable request idempotency.
2. An agentic orchestration foundation that coordinates engineering work through
   versioned dependency graphs, execution gates, parallel paths, synchronization,
   context propagation, and bounded retries.

The long-term objective is to transform an engineering requirement into a
reviewable and validated engineering outcome under controlled agent autonomy.

## Current status

Implemented through Commit 4:

- Core URL-shortening APIs
- Durable URL-creation idempotency
- Versioned engineering workflow model
- Dependency-graph validation and execution
- Parallel task execution and join synchronization
- Entry and exit gates
- Shared cross-stage execution context
- Bounded task retries

The next stage connects the orchestration engine to requirement-analysis,
planning, repository-reasoning, implementation, testing, and validation agents.

## Implemented capabilities

- Deterministic requirement normalization
- Acceptance-criteria and assumption generation
- Ambiguity detection and clarification pause
- Requirement risk classification
- Scenario-aware dynamic workflow planning
- Different greenfield and brownfield dependency graphs
- Documentation-only workflow optimization
- High-risk security-review planning
- Automatic engineering-workflow execution API
- Workflow inspection API

### URL shortener

- HTTP and HTTPS URL validation
- URL normalization
- Collision-resistant Base62 short-code generation
- Database-enforced short-code uniqueness
- Optional URL expiration
- Active and disabled URL states
- URL creation API
- URL details API
- Public redirect API
- PostgreSQL persistence
- Flyway-controlled schema migrations
- RFC 9457 Problem Details responses

### Durable idempotency

- Required `Idempotency-Key` header for URL creation
- PostgreSQL-backed idempotency reservations
- SHA-256 request fingerprinting
- Same key and same payload replay the original resource
- Same key with a different payload returns `409 Conflict`
- In-progress duplicate detection
- Recovery of failed or timed-out reservations
- Stored original response status
- `Idempotency-Replayed` response header
- Database locking for concurrent reservation handling

### Agentic orchestration foundation

- Versioned engineering workflows
- Explicit directed acyclic dependency graph
- Missing-dependency validation
- Cycle detection
- Sequential task execution
- Parallel task execution
- Join synchronization across parallel branches
- Entry and exit gates
- Context-key gates
- Revision-bound human-approval gate model
- Thread-safe cross-stage execution context
- Automatic task execution through handler contracts
- Bounded task retries
- Safe workflow status transitions
- In-memory workflow repository
- Virtual-thread task execution

### Testing

- Application-context test
- URL-domain behavior tests
- URL-validation tests
- Short-code generation tests
- URL service tests
- Idempotency-domain tests
- Request-fingerprint tests
- Workflow graph-validation tests
- Parallel execution and synchronization tests
- Bounded retry tests

## Architecture

```text
┌──────────────────────────────────────────────┐
│ Client                                       │
└───────────────────┬──────────────────────────┘
                    │
                    ▼
┌──────────────────────────────────────────────┐
│ Spring MVC API                               │
│                                              │
│ POST /api/v1/urls                            │
│ GET  /api/v1/urls/{shortCode}                │
│ GET  /{shortCode}                            │
└───────────────┬──────────────────────────────┘
                │
                ▼
┌──────────────────────────────────────────────┐
│ Application services                         │
│                                              │
│ IdempotentUrlCreationService                 │
│ ShortUrlService                              │
│ OriginalUrlValidator                         │
│ SecureRandomShortCodeGenerator               │
└───────────────┬──────────────────────────────┘
                │
                ▼
┌──────────────────────────────────────────────┐
│ PostgreSQL                                   │
│                                              │
│ short_urls                                   │
│ idempotency_records                          │
│ flyway_schema_history                        │
└──────────────────────────────────────────────┘
```

Agentic workflow foundation:

```text
Engineering requirement
        │
        ▼
EngineeringWorkflow
        │
        ▼
WorkflowGraphValidator
        │
        ▼
WorkflowEngine
        │
        ├── Sequential tasks
        │
        ├── Parallel task wave
        │       ├── Implementation
        │       └── Test planning
        │
        ├── Join synchronization
        │
        ├── Entry and exit gates
        │
        ├── Shared execution context
        │
        └── Bounded retry
                │
                ▼
WorkflowTaskHandler
```

At this stage, `WorkflowTaskHandler` is the extension point for the real
engineering agents introduced in subsequent commits.

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
- GitHub Actions planned for the final CI commit

## Project structure

```text
src/main/java/com/navya/agentic_url_shortener/
├── agent/
├── artifact/
├── audit/
├── common/
├── config/
├── governance/
├── idempotency/
│   ├── domain/
│   ├── dto/
│   ├── exception/
│   ├── repository/
│   └── service/
├── orchestration/
│   ├── domain/
│   ├── engine/
│   ├── exception/
│   ├── gate/
│   └── repository/
├── policy/
├── tool/
└── url/
    ├── controller/
    ├── domain/
    ├── dto/
    ├── exception/
    ├── repository/
    └── service/
```

## Prerequisites

Install:

- JDK 21
- Docker Desktop
- Git

Maven installation is not required because the Maven Wrapper is included.

Verify Java:

```powershell
java -version
```

Verify Docker:

```powershell
docker version
docker compose version
```

## Local development

### 1. Start PostgreSQL

Ensure Docker Desktop is running.

From the project root:

```powershell
docker compose up -d postgres
docker compose ps
```

Wait until this container is healthy:

```text
agentic-url-shortener-postgres
```

View PostgreSQL logs if necessary:

```powershell
docker compose logs postgres
```

### 2. Run all tests

```powershell
.\mvnw.cmd clean test
```

A successful run ends with:

```text
BUILD SUCCESS
```

### 3. Start the application

```powershell
.\mvnw.cmd spring-boot:run
```

The application starts at:

```text
http://localhost:8080
```

Flyway automatically applies all pending migrations before Hibernate validates
the database schema.

### 4. Check application health

```powershell
Invoke-RestMethod `
    -Method Get `
    -Uri "http://localhost:8080/actuator/health"
```

Expected response:

```json
{
  "status": "UP"
}
```

## API

### Create a short URL

```http
POST /api/v1/urls
Content-Type: application/json
Idempotency-Key: request-123
```

Request:

```json
{
  "url": "https://example.com/products?id=10",
  "expiresAt": "2027-08-13T12:00:00Z"
}
```

First response:

```http
HTTP/1.1 201 Created
Location: /api/v1/urls/Ab12Cd34
Idempotency-Replayed: false
```

```json
{
  "id": "generated-uuid",
  "shortCode": "Ab12Cd34",
  "shortUrl": "http://localhost:8080/Ab12Cd34",
  "originalUrl": "https://example.com/products?id=10",
  "status": "ACTIVE",
  "createdAt": "2026-08-13T12:00:00Z",
  "expiresAt": "2027-08-13T12:00:00Z"
}
```

Repeating the same key and payload returns the same resource:

```http
HTTP/1.1 201 Created
Idempotency-Replayed: true
```

Using the same key with a different payload returns:

```http
HTTP/1.1 409 Conflict
```

### Retrieve URL details

```http
GET /api/v1/urls/{shortCode}
```

Example:

```powershell
Invoke-RestMethod `
    -Method Get `
    -Uri "http://localhost:8080/api/v1/urls/Ab12Cd34"
```

### Redirect to the original URL

```http
GET /{shortCode}
```

An active short URL returns:

```http
HTTP/1.1 302 Found
Location: https://example.com/products?id=10
```

An expired or disabled short URL returns:

```http
HTTP/1.1 410 Gone
```

An unknown short code returns:

```http
HTTP/1.1 404 Not Found
```

## API testing

### Idempotent creation

Create an idempotency key:

```powershell
$key = [guid]::NewGuid().ToString()
```

Create the request body:

```powershell
$body = '{
  "url": "https://example.com/idempotent",
  "expiresAt": null
}'
```

Send the first request:

```powershell
$first = Invoke-WebRequest `
    -Method Post `
    -Uri "http://localhost:8080/api/v1/urls" `
    -Headers @{
        "Idempotency-Key" = $key
    } `
    -ContentType "application/json" `
    -Body $body

$first.StatusCode
$first.Headers["Idempotency-Replayed"]
$first.Content
```

Expected:

```text
201
false
```

Replay the same request:

```powershell
$second = Invoke-WebRequest `
    -Method Post `
    -Uri "http://localhost:8080/api/v1/urls" `
    -Headers @{
        "Idempotency-Key" = $key
    } `
    -ContentType "application/json" `
    -Body $body

$second.StatusCode
$second.Headers["Idempotency-Replayed"]
$second.Content
```

Expected:

```text
201
true
```

Verify both responses reference the same resource:

```powershell
$firstJson = $first.Content | ConvertFrom-Json
$secondJson = $second.Content | ConvertFrom-Json

$firstJson.id -eq $secondJson.id
$firstJson.shortCode -eq $secondJson.shortCode
```

Expected:

```text
True
True
```

### Idempotency conflict

Reuse the same key with a different payload:

```powershell
try {
    Invoke-RestMethod `
        -Method Post `
        -Uri "http://localhost:8080/api/v1/urls" `
        -Headers @{
            "Idempotency-Key" = $key
        } `
        -ContentType "application/json" `
        -Body '{
          "url": "https://different.example.com"
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

### Missing idempotency key

```powershell
try {
    Invoke-RestMethod `
        -Method Post `
        -Uri "http://localhost:8080/api/v1/urls" `
        -ContentType "application/json" `
        -Body $body
} catch {
    $_.Exception.Response.StatusCode
    $_.ErrorDetails.Message
}
```

Expected:

```text
400 Bad Request
```

### Redirect testing

```powershell
$shortCode = $firstJson.shortCode
curl.exe -i "http://localhost:8080/$shortCode"
```

Expected:

```text
HTTP/1.1 302
Location: https://example.com/idempotent
```

## Problem Details

API errors use RFC 9457 Problem Details.

Example:

```json
{
  "type": "https://agentic-url-shortener.dev/problems/idempotency-conflict",
  "title": "Idempotency conflict",
  "status": 409,
  "detail": "Idempotency key was already used with a different request",
  "instance": "/api/v1/urls",
  "timestamp": "2026-08-13T12:00:00Z"
}
```

Supported error cases currently include:

- Invalid URL
- Request validation failure
- Unknown short URL
- Expired or disabled short URL
- Short-code allocation failure
- Invalid idempotency key
- Idempotency fingerprint conflict
- Request already in progress

## Database inspection

List tables:

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

Inspect shortened URLs:

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
    -c "SELECT idempotency_key, status, resource_id, response_status, expires_at FROM idempotency_records;"
```

## Configuration

Default application settings:

| Setting | Environment variable | Default |
|---|---|---|
| Server port | `SERVER_PORT` | `8080` |
| Database URL | `DB_URL` | `jdbc:postgresql://localhost:5432/agentic_url_shortener` |
| Database username | `DB_USERNAME` | `postgres` |
| Database password | `DB_PASSWORD` | `postgres` |
| Public short URL base | `SHORT_URL_BASE_URL` | `http://localhost:8080` |
| Short-code length | `SHORT_CODE_LENGTH` | `8` |
| Code-generation attempts | `SHORT_CODE_GENERATION_ATTEMPTS` | `10` |
| Idempotency retention | `IDEMPOTENCY_RETENTION` | `PT24H` |
| In-progress timeout | `IDEMPOTENCY_IN_PROGRESS_TIMEOUT` | `PT2M` |
| Maximum key length | `IDEMPOTENCY_MAX_KEY_LENGTH` | `128` |
| Agent workspace | `AGENT_WORKSPACE_ROOT` | `./agent-workspaces` |
| Agent maximum attempts | `AGENT_MAX_ATTEMPTS` | `2` |
| Command timeout | `AGENT_COMMAND_TIMEOUT_SECONDS` | `120` |
| Model provider | `MODEL_PROVIDER` | `deterministic` |

## Stop the local environment

Stop PostgreSQL while retaining its data:

```powershell
docker compose stop postgres
```

Restart it:

```powershell
docker compose start postgres
```

Stop and remove the container while retaining its named volume:

```powershell
docker compose down
```

Do not run the following unless the local PostgreSQL data should be permanently
deleted:

```powershell
docker compose down -v
```

## Design decisions

### Database-backed idempotency

Idempotency state is stored in PostgreSQL rather than memory so behavior survives
application restarts and works across multiple application instances.

The idempotency key is the record primary key. The request payload is represented
by a SHA-256 fingerprint. Completed records reference the created URL and retain
the original HTTP response status.

### Schema-controlled persistence

Hibernate uses:

```text
ddl-auto=validate
```

Flyway owns schema changes. Hibernate verifies that application mappings match
the migrated schema without creating or modifying production tables.

### Derived expiration state

Expiration is evaluated from `expiresAt` and the current UTC clock. It is not
stored as a separate status because a persisted `EXPIRED` value could become
stale without a scheduled update.

### Controlled domain models

Lombok is used for constructor injection and DTO boilerplate. JPA entities do not
use Lombok `@Data`, avoiding unsafe generated equality, hashing, and string
behavior over mutable persistence fields.

### Explicit dependency graph

Workflow dependencies are represented by task identifiers. Graph validation
rejects missing references and cycles before execution begins.

### Parallel execution with synchronization

Tasks whose dependencies have succeeded become runnable together. They execute
in a parallel wave using virtual threads. A downstream join task becomes runnable
only after every declared dependency succeeds.

### Revision-bound approvals

Human-approval gates use a context key containing the workflow revision. A later
replan increments the revision, making prior approval evidence invalid for the
new workflow state.

### Requirement understanding and dynamic planning

The orchestration system performs deterministic and reproducible requirement
analysis before executing engineering stages.

Implemented analysis includes:

- Requirement normalization
- Acceptance-criteria generation
- Ambiguity detection
- Assumption recording
- Risk classification
- Documentation-only change detection
- Greenfield and brownfield classification

Plans are generated according to the requirement rather than using one fixed
workflow:

- Greenfield work begins with requirement and architecture analysis.
- Brownfield work adds repository-impact analysis.
- Documentation-only work avoids implementation tasks.
- High-risk work adds a security-review task.
- Ambiguous work pauses at `AWAITING_CLARIFICATION`.

Current stage handlers generate structured deterministic outputs. Controlled
repository inspection and real engineering artifacts are added in subsequent
commits.

### Create an engineering workflow

```http
POST /api/v1/engineering-workflows
Content-Type: application/json
```

Brownfield example:

```json
{
  "scenarioType": "BROWNFIELD",
  "requirement": "Add redirect analytics to the existing URL API",
  "repositoryPath": "./repository"
}
```

Greenfield example:

```json
{
  "scenarioType": "GREENFIELD",
  "requirement": "Create a URL shortening API with expiration",
  "repositoryPath": null
}
```

Ambiguous example:

```json
{
  "scenarioType": "AMBIGUOUS",
  "requirement": "Make shortened links safer and better",
  "repositoryPath": null
}
```

The ambiguous workflow stops before architecture or implementation and returns:

```text
AWAITING_CLARIFICATION
```

### Retrieve an engineering workflow

```http
GET /api/v1/engineering-workflows/{workflowId}
```

Workflow retrieval is currently available for the lifetime of the running
application because workflow persistence is still in memory.

## Current limitations

- Workflow state is currently held in memory.
- Requirement analysis is deterministic rather than model-backed.
- Non-requirement handlers currently produce structured stage outputs rather than repository patches.
- Repository inspection tools are introduced in the next commit.
- Engineering artifacts and audit events are not persisted yet.
- Clarification submission and workflow replanning are introduced later.
- Redirect analytics are not implemented yet.
- Authentication and authorization are not implemented yet.
- GitHub Actions CI is added in the final infrastructure commit.

## Planned next capabilities

- Requirement normalization
- Ambiguity detection
- Scenario-aware dynamic planning
- Brownfield repository reasoning
- Architecture artifact generation
- Implementation and test patch generation
- Executable validation gates
- Dynamic replanning
- Rollback and safe stop
- Policy-backed approvals
- Audit-grade observability
- Reliability metrics
- Greenfield, brownfield, and ambiguous scenario demonstrations
- GitHub Actions quality gates