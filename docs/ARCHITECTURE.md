# Architecture

## Overview

The application demonstrates controlled agentic software-engineering
orchestration through a production-oriented URL-shortening service.

It contains two primary bounded capabilities:

1. A URL-shortening API with durable idempotency.
2. An engineering workflow engine that converts requirements into planned,
   validated, governed, and auditable outcomes.

## Component view

```text
Client
  |
  +-- URL API
  |     |
  |     +-- Idempotency Service
  |     +-- Short URL Service
  |     +-- PostgreSQL
  |
  +-- Engineering Workflow API
        |
        +-- Requirement Analyzer
        +-- Scenario-Aware Planner
        +-- Dependency Graph Engine
        +-- Repository Inspector
        +-- Artifact Store
        +-- Controlled Maven Tool
        +-- Policy Evaluator
        +-- Governance Service
        +-- Audit Journal
        +-- Micrometer Metrics
```

## Agentic execution flow

```text
Requirement
    |
    v
Requirement analysis
    |
    +-- ambiguous --> Await clarification or safe stop
    |
    v
Scenario-aware planning
    |
    v
Dependency graph validation
    |
    v
Repository and architecture analysis
    |
    v
Implementation and test planning
    |
    v
Controlled Maven validation
    |
    v
Policy evaluation
    |
    v
Human approval
    |
    v
Release-readiness outcome
```

## Safety boundaries

### Repository boundary

Repository paths must remain within `AGENT_REPOSITORY_ROOT`. Path traversal,
symbolic-link escape, excessive file counts, and oversized files are rejected.

### Command boundary

The workflow can only run the repository Maven Wrapper with fixed,
application-controlled arguments. API clients cannot provide arbitrary commands
or arguments.

### Approval boundary

Release-readiness approval is bound to the workflow revision and current
artifact hashes. An earlier approval cannot authorize changed evidence.

### Idempotency boundary

URL creation uses a PostgreSQL-backed reservation and request fingerprint.
Equivalent retries replay the original response, while conflicting payloads are
rejected.

## Persistence

PostgreSQL stores:

- Short URLs
- Idempotency records
- Flyway schema history
- Platform metadata

Workflow state, artifact references, and audit events currently use in-memory
repositories. Generated artifact files remain on disk under the configured
agent workspace.

## Observability

Micrometer exposes:

- Workflow lifecycle counters
- Task outcome counters
- Retry counters
- Clarification counters
- Approval counters
- Safe-stop counters
- Workflow-duration timers

Structured workflow audit events provide workflow-specific traceability.
Metrics provide aggregate operational visibility.

Workflow IDs are deliberately excluded from metric tags to avoid
high-cardinality telemetry.

## Deployment

Docker Compose runs:

- PostgreSQL 17
- The Spring Boot application

The application container runs as a non-root user. Health checks gate startup
and report application readiness through Spring Boot Actuator.

GitHub Actions runs:

1. Java 21 setup
2. Maven tests
3. JaCoCo report and coverage gate
4. Test-report upload on failure
5. Docker Compose validation
6. Docker image build validation

## Known limitations

- Workflow and audit repositories are in memory.
- Requirement analysis uses a deterministic provider.
- Implementation results are reviewable plans rather than applied patches.
- Clarification submission and replanning are not implemented.
- Authentication uses a prototype `X-Actor` boundary.
- Persistent audit storage and distributed metrics collection are not included.