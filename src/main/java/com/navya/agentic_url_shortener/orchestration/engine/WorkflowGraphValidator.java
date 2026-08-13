package com.navya.agentic_url_shortener.orchestration.engine;

import com.navya.agentic_url_shortener.orchestration.domain.EngineeringWorkflow;
import com.navya.agentic_url_shortener.orchestration.domain.WorkflowTask;
import com.navya.agentic_url_shortener.orchestration.exception.InvalidWorkflowGraphException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class WorkflowGraphValidator {

    private enum VisitState {
        VISITING,
        VISITED
    }

    public void validate(EngineeringWorkflow workflow) {
        if (workflow.getTasks().isEmpty()) {
            throw new InvalidWorkflowGraphException(
                    "Workflow must contain at least one task"
            );
        }

        validateDependenciesExist(workflow);
        validateAcyclic(workflow);
    }

    private void validateDependenciesExist(
            EngineeringWorkflow workflow
    ) {
        for (WorkflowTask task : workflow.getTasks()) {
            for (UUID dependencyId :
                    task.getDependencyIds()) {
                try {
                    workflow.requireTask(dependencyId);
                } catch (IllegalArgumentException exception) {
                    throw new InvalidWorkflowGraphException(
                            "Task "
                                    + task.getId()
                                    + " references missing dependency "
                                    + dependencyId
                    );
                }
            }
        }
    }

    private void validateAcyclic(
            EngineeringWorkflow workflow
    ) {
        Map<UUID, VisitState> states = new HashMap<>();

        for (WorkflowTask task : workflow.getTasks()) {
            visit(workflow, task, states);
        }
    }

    private void visit(
            EngineeringWorkflow workflow,
            WorkflowTask task,
            Map<UUID, VisitState> states
    ) {
        VisitState current = states.get(task.getId());

        if (current == VisitState.VISITING) {
            throw new InvalidWorkflowGraphException(
                    "Workflow dependency graph contains a cycle"
            );
        }

        if (current == VisitState.VISITED) {
            return;
        }

        states.put(task.getId(), VisitState.VISITING);

        for (UUID dependencyId :
                task.getDependencyIds()) {
            visit(
                    workflow,
                    workflow.requireTask(dependencyId),
                    states
            );
        }

        states.put(task.getId(), VisitState.VISITED);
    }
}