# Agentic URL Shortener

A production-oriented URL-shortening service and agentic software-engineering
orchestration prototype.

The platform is designed to transform an engineering requirement into a
reviewable and validated outcome using controlled agent autonomy.

## Implemented capabilities

- HTTP and HTTPS URL validation
- Collision-resistant Base62 short-code generation
- Optional URL expiration
- URL creation and retrieval APIs
- Public redirect API
- PostgreSQL persistence
- Flyway-controlled database migrations
- RFC 9457 Problem Details responses
- Unit tests for domain and service behavior
- Docker Compose PostgreSQL environment

## Planned capabilities

- Durable, database-backed request idempotency
- Requirement understanding and ambiguity detection
- Dynamic dependency-graph planning
- Brownfield repository reasoning
- Architecture, implementation, testing and documentation artifacts
- Executable validation gates
- Bounded retries and dynamic replanning
- Policy-backed human approvals
- Rollback and safe-stop controls
- Audit-grade traceability
- Automated CI quality gates

## Technology

- Java 21
- Spring Boot 4.1.0
- Spring MVC
- PostgreSQL 17
- Spring Data JPA
- Hibernate
- Flyway
- Maven
- JUnit
- Mockito
- Docker Compose
- GitHub Actions

## API

### Create a short URL

```http
POST /api/v1/urls
Content-Type: application/json
```

Request:

```json
{
  "url": "https://example.com/products?id=10",
  "expiresAt": "2027-08-13T12:00:00Z"
}
```

Response:

```http
HTTP/1.1 201 Created
Location: /api/v1/urls/Ab12Cd34
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

### Retrieve URL details

```http
GET /api/v1/urls/{shortCode}
```

### Redirect to the original URL

```http
GET /{shortCode}
```

A valid and active short code returns:

```http
HTTP/1.1 302 Found
Location: https://example.com/products?id=10
```

## Local development

### Start PostgreSQL

Docker Desktop must be running.

```powershell
docker compose up -d postgres
docker compose ps
```

Wait until PostgreSQL reports `healthy`.

### Start the application

```powershell
.\mvnw.cmd spring-boot:run
```

The application starts at:

```text
http://localhost:8080
```

### Verify application health

```powershell
Invoke-RestMethod `
    -Uri "http://localhost:8080/actuator/health"
```

Expected response:

```json
{
  "status": "UP"
}
```

## API testing

### Create a shortened URL

```powershell
$response = Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:8080/api/v1/urls" `
    -ContentType "application/json" `
    -Body '{
      "url": "https://example.com/products?id=10",
      "expiresAt": "2027-08-13T12:00:00Z"
    }'

$response
```

Save the generated short code:

```powershell
$shortCode = $response.shortCode
```

Retrieve the URL details:

```powershell
Invoke-RestMethod `
    -Method Get `
    -Uri "http://localhost:8080/api/v1/urls/$shortCode"
```

Verify the redirect:

```powershell
curl.exe -i "http://localhost:8080/$shortCode"
```

Expected response:

```text
HTTP/1.1 302
Location: https://example.com/products?id=10
```

### Test invalid URL validation

```powershell
try {
    Invoke-RestMethod `
        -Method Post `
        -Uri "http://localhost:8080/api/v1/urls" `
        -ContentType "application/json" `
        -Body '{
          "url": "ftp://example.com/file"
        }'
} catch {
    $_.ErrorDetails.Message
}
```

Expected status:

```text
400 Bad Request
```

### Test an unknown short code

```powershell
try {
    Invoke-RestMethod `
        -Method Get `
        -Uri "http://localhost:8080/api/v1/urls/Missing1"
} catch {
    $_.ErrorDetails.Message
}
```

Expected status:

```text
404 Not Found
```

## Testing

Run all tests on Windows:

```powershell
.\mvnw.cmd clean test
```

Run all tests on Linux or macOS:

```bash
./mvnw clean test
```

A successful build ends with:

```text
BUILD SUCCESS
```

## Database inspection

List the PostgreSQL tables:

```powershell
docker exec `
    agentic-url-shortener-postgres `
    psql `
    -U postgres `
    -d agentic_url_shortener `
    -c "\dt"
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

## Stop the local environment

Stop PostgreSQL while preserving its data:

```powershell
docker compose stop postgres
```

Restart it later:

```powershell
docker compose start postgres
```

Stop and remove the container while preserving the database volume:

```powershell
docker compose down
```

Do not use `docker compose down -v` unless the local database data should be deleted.

## Current limitations

- URL creation is not idempotent yet.
- Redirect analytics are not implemented yet.
- The agentic orchestration engine is not implemented yet.
- Authentication and authorization are not implemented yet.
- The application image is not yet included in Docker Compose.