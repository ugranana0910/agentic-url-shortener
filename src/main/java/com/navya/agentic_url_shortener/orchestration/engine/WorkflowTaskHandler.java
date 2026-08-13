package com.navya.agentic_url_shortener.orchestration.engine;

import com.navya.agentic_url_shortener.orchestration.domain.EngineeringWorkflow;
import com.navya.agentic_url_shortener.orchestration.domain.TaskType;
import com.navya.agentic_url_shortener.orchestration.domain.WorkflowTask;

public interface WorkflowTaskHandler {

    TaskType supports();

    TaskExecutionResult execute(
            EngineeringWorkflow workflow,
            WorkflowTask task
    );
}