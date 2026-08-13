package com.navya.agentic_url_shortener.orchestration.domain;

public enum WorkflowStatus {
    CREATED,
    RUNNING,
    AWAITING_CLARIFICATION,
    AWAITING_APPROVAL,
    COMPLETED,
    FAILED,
    SAFE_STOPPED
}