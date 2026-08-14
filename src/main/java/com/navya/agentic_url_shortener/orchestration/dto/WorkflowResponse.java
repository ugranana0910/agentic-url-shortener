package com.navya.agentic_url_shortener.orchestration.dto;

import com.navya.agentic_url_shortener.orchestration.domain.EngineeringWorkflow;
import com.navya.agentic_url_shortener.orchestration.domain.ScenarioType;
import com.navya.agentic_url_shortener.orchestration.domain.WorkflowStatus;
import lombok.Value;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Value
public class WorkflowResponse {

    UUID id;
    ScenarioType scenarioType;
    String requirement;
    String repositoryPath;
    int revision;
    WorkflowStatus status;
    Instant createdAt;
    Instant startedAt;
    Instant completedAt;
    String failureMessage;
    List<WorkflowTaskResponse> tasks;
    Map<String, Object> context;

    public static WorkflowResponse from(
            EngineeringWorkflow workflow
    ) {
        List<WorkflowTaskResponse> tasks =
                workflow.getTasks()
                        .stream()
                        .map(WorkflowTaskResponse::from)
                        .toList();

        return new WorkflowResponse(
                workflow.getId(),
                workflow.getScenarioType(),
                workflow.getRequirement(),
                workflow.getRepositoryPath(),
                workflow.getRevision(),
                workflow.getStatus(),
                workflow.getCreatedAt(),
                workflow.getStartedAt(),
                workflow.getCompletedAt(),
                workflow.getFailureMessage(),
                tasks,
                workflow.getContext().snapshot()
        );
    }
}