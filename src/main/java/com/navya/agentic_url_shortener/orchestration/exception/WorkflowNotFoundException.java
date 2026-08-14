package com.navya.agentic_url_shortener.orchestration.exception;

import java.util.UUID;

public class WorkflowNotFoundException
        extends RuntimeException {

    public WorkflowNotFoundException(UUID workflowId) {
        super("Engineering workflow not found: " + workflowId);
    }
}