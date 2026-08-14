package com.navya.agentic_url_shortener.orchestration.service;

import com.navya.agentic_url_shortener.orchestration.domain.EngineeringWorkflow;
import com.navya.agentic_url_shortener.orchestration.dto.CreateWorkflowRequest;
import com.navya.agentic_url_shortener.orchestration.dto.WorkflowResponse;
import com.navya.agentic_url_shortener.orchestration.engine.WorkflowEngine;
import com.navya.agentic_url_shortener.orchestration.exception.WorkflowNotFoundException;
import com.navya.agentic_url_shortener.orchestration.planner.PlannedWorkflow;
import com.navya.agentic_url_shortener.orchestration.planner.WorkflowPlanner;
import com.navya.agentic_url_shortener.orchestration.repository.WorkflowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EngineeringWorkflowService {

    private final WorkflowPlanner workflowPlanner;
    private final WorkflowEngine workflowEngine;
    private final WorkflowRepository workflowRepository;

    public WorkflowResponse createAndExecute(
            CreateWorkflowRequest request
    ) {
        PlannedWorkflow planned =
                workflowPlanner.plan(
                        request.getScenarioType(),
                        request.getRequirement(),
                        request.getRepositoryPath()
                );

        EngineeringWorkflow executed =
                workflowEngine.execute(
                        planned.getWorkflow()
                );

        return WorkflowResponse.from(executed);
    }

    public WorkflowResponse get(UUID workflowId) {
        EngineeringWorkflow workflow =
                workflowRepository.findById(workflowId)
                        .orElseThrow(
                                () -> new WorkflowNotFoundException(
                                        workflowId
                                )
                        );

        return WorkflowResponse.from(workflow);
    }
}