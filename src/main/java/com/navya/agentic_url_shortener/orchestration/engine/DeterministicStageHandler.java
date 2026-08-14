package com.navya.agentic_url_shortener.orchestration.engine;

import com.navya.agentic_url_shortener.orchestration.domain.EngineeringWorkflow;
import com.navya.agentic_url_shortener.orchestration.domain.TaskType;
import com.navya.agentic_url_shortener.orchestration.domain.WorkflowTask;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class DeterministicStageHandler
        implements WorkflowTaskHandler {

    private final TaskType taskType;
    private final String outputContextKey;

    public DeterministicStageHandler(
            TaskType taskType,
            String outputContextKey
    ) {
        this.taskType = Objects.requireNonNull(taskType);
        this.outputContextKey =
                Objects.requireNonNull(outputContextKey);
    }

    @Override
    public TaskType supports() {
        return taskType;
    }

    @Override
    public TaskExecutionResult execute(
            EngineeringWorkflow workflow,
            WorkflowTask task
    ) {
        Map<String, Object> output =
                new LinkedHashMap<>();

        output.put("taskId", task.getId().toString());
        output.put("taskType", taskType.name());
        output.put(
                "workflowRevision",
                workflow.getRevision()
        );
        output.put(
                "normalizedRequirement",
                workflow.getContext()
                        .get("normalizedRequirement")
                        .orElse(workflow.getRequirement())
        );
        output.put(
                "outcome",
                "Deterministic stage completed"
        );

        return TaskExecutionResult.of(
                outputContextKey,
                output
        );
    }
}