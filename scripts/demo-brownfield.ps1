param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$Actor = "assessment-reviewer"
)

$ErrorActionPreference = "Stop"

Write-Host "Running BROWNFIELD scenario" -ForegroundColor Cyan

$workflow = Invoke-RestMethod `
    -Method Post `
    -Uri "$BaseUrl/api/v1/engineering-workflows" `
    -ContentType "application/json" `
    -Body '{
      "scenarioType": "BROWNFIELD",
      "requirement": "Add redirect analytics to the existing URL API",
      "repositoryPath": "."
    }'

if ($workflow.status -ne "AWAITING_APPROVAL") {
    throw "Expected AWAITING_APPROVAL, got $($workflow.status)"
}

if (-not (
    $workflow.tasks.type -contains "REPOSITORY_ANALYSIS"
)) {
    throw "Brownfield workflow is missing repository analysis"
}

$artifacts = Invoke-RestMethod `
    -Method Get `
    -Uri "$BaseUrl/api/v1/engineering-workflows/$($workflow.id)/artifacts"

$requiredArtifacts = @(
    "repository-analysis.md",
    "architecture.md",
    "implementation-plan.md",
    "test-plan.md",
    "maven-test-attempt-1.log",
    "validation-report-attempt-1.md"
)

foreach ($name in $requiredArtifacts) {
    if (-not ($artifacts.name -contains $name)) {
        throw "Missing artifact: $name"
    }
}

$policies = Invoke-RestMethod `
    -Method Get `
    -Uri "$BaseUrl/api/v1/engineering-workflows/$($workflow.id)/governance/policies"

if ($policies.passed -contains $false) {
    throw "One or more release policies failed"
}

$approved = Invoke-RestMethod `
    -Method Post `
    -Uri "$BaseUrl/api/v1/engineering-workflows/$($workflow.id)/governance/approvals/release-readiness" `
    -Headers @{
        "X-Actor" = $Actor
    } `
    -ContentType "application/json" `
    -Body '{
      "reason": "Brownfield validation and artifacts reviewed"
    }'

if ($approved.status -ne "COMPLETED") {
    throw "Expected COMPLETED, got $($approved.status)"
}

$auditEvents = Invoke-RestMethod `
    -Method Get `
    -Uri "$BaseUrl/api/v1/engineering-workflows/$($workflow.id)/audit-events"

Write-Host "Workflow: $($workflow.id)"
Write-Host "Artifacts: $($artifacts.Count)"
Write-Host "Audit events: $($auditEvents.Count)"
Write-Host "BROWNFIELD scenario passed" -ForegroundColor Green