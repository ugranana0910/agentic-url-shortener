package com.navya.agentic_url_shortener.orchestration.dto;

import com.navya.agentic_url_shortener.orchestration.domain.GateType;
import com.navya.agentic_url_shortener.orchestration.domain.TaskStatus;
import com.navya.agentic_url_shortener.orchestration.domain.TaskType;
import com.navya.agentic_url_shortener.orchestration.domain.WorkflowTask;
import lombok.Value;

import java.util.Set;
import java.util.UUID;

@Value
public class WorkflowTaskResponse {

    UUID id;
    String name;
    TaskType type;
    TaskStatus status;
    Set<UUID> dependencyIds;
    GateType entryGate;
    GateType exitGate;
    int attempt;
    int maxAttempts;
    String failureMessage;

    public static WorkflowTaskResponse from(
            WorkflowTask task
    ) {
        return new WorkflowTaskResponse(
                task.getId(),
                task.getName(),
                task.getType(),
                task.getStatus(),
                Set.copyOf(task.getDependencyIds()),
                task.getEntryGate().getType(),
                task.getExitGate().getType(),
                task.getAttempt(),
                task.getMaxAttempts(),
                task.getFailureMessage()
        );
    }
}