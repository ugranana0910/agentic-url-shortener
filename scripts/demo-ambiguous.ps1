param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$Actor = "assessment-reviewer"
)

$ErrorActionPreference = "Stop"

Write-Host "Running AMBIGUOUS scenario" -ForegroundColor Cyan

$workflow = Invoke-RestMethod `
    -Method Post `
    -Uri "$BaseUrl/api/v1/engineering-workflows" `
    -ContentType "application/json" `
    -Body '{
      "scenarioType": "AMBIGUOUS",
      "requirement": "Make shortened links safer and better",
      "repositoryPath": null
    }'

if ($workflow.status -ne "AWAITING_CLARIFICATION") {
    throw "Expected AWAITING_CLARIFICATION, got $($workflow.status)"
}

if ($workflow.tasks.type -contains "IMPLEMENTATION") {
    throw "Implementation must not start for ambiguous work"
}

if ($workflow.context.ambiguities.Count -lt 1) {
    throw "Expected at least one clarification question"
}

$stopped = Invoke-RestMethod `
    -Method Post `
    -Uri "$BaseUrl/api/v1/engineering-workflows/$($workflow.id)/governance/safe-stop" `
    -Headers @{
        "X-Actor" = $Actor
    } `
    -ContentType "application/json" `
    -Body '{
      "reason": "Clarification unavailable during deterministic demonstration"
    }'

if ($stopped.status -ne "SAFE_STOPPED") {
    throw "Expected SAFE_STOPPED, got $($stopped.status)"
}

$auditEvents = Invoke-RestMethod `
    -Method Get `
    -Uri "$BaseUrl/api/v1/engineering-workflows/$($workflow.id)/audit-events"

if (-not (
    $auditEvents.type -contains "CLARIFICATION_REQUIRED"
)) {
    throw "Audit trail is missing CLARIFICATION_REQUIRED"
}

if (-not (
    $auditEvents.type -contains "SAFE_STOPPED"
)) {
    throw "Audit trail is missing SAFE_STOPPED"
}

Write-Host "Workflow: $($workflow.id)"
Write-Host "Ambiguities: $($workflow.context.ambiguities.Count)"
Write-Host "Audit events: $($auditEvents.Count)"
Write-Host "AMBIGUOUS scenario passed" -ForegroundColor Green