package com.navya.agentic_url_shortener.orchestration.domain;

public enum GateType {
    NONE,
    DEPENDENCIES_SUCCEEDED,
    CONTEXT_KEY_PRESENT,
    HUMAN_APPROVAL
}