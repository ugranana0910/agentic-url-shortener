param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$Actor = "assessment-reviewer"
)

$ErrorActionPreference = "Stop"

Write-Host "Running GREENFIELD scenario" -ForegroundColor Cyan

$workflow = Invoke-RestMethod `
    -Method Post `
    -Uri "$BaseUrl/api/v1/engineering-workflows" `
    -ContentType "application/json" `
    -Body '{
      "scenarioType": "GREENFIELD",
      "requirement": "Create a URL shortening API with expiration",
      "repositoryPath": null
    }'

if ($workflow.status -ne "AWAITING_APPROVAL") {
    throw "Expected AWAITING_APPROVAL, got $($workflow.status)"
}

if ($workflow.tasks.type -contains "REPOSITORY_ANALYSIS") {
    throw "Greenfield workflow unexpectedly contains repository analysis"
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
      "reason": "Greenfield artifacts and policies reviewed"
    }'

if ($approved.status -ne "COMPLETED") {
    throw "Expected COMPLETED, got $($approved.status)"
}

$auditEvents = Invoke-RestMethod `
    -Method Get `
    -Uri "$BaseUrl/api/v1/engineering-workflows/$($workflow.id)/audit-events"

Write-Host "Workflow: $($workflow.id)"
Write-Host "Tasks: $($approved.tasks.Count)"
Write-Host "Audit events: $($auditEvents.Count)"
Write-Host "GREENFIELD scenario passed" -ForegroundColor Green