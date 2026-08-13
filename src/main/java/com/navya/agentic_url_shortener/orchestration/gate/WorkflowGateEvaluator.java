package com.navya.agentic_url_shortener.orchestration.gate;

import com.navya.agentic_url_shortener.orchestration.domain.EngineeringWorkflow;
import com.navya.agentic_url_shortener.orchestration.domain.GateDefinition;
import com.navya.agentic_url_shortener.orchestration.domain.GateType;
import com.navya.agentic_url_shortener.orchestration.domain.TaskStatus;
import com.navya.agentic_url_shortener.orchestration.domain.WorkflowTask;
import org.springframework.stereotype.Component;

@Component
public class WorkflowGateEvaluator {

    public boolean entryGatePassed(
            EngineeringWorkflow workflow,
            WorkflowTask task
    ) {
        return evaluate(
                workflow,
                task,
                task.getEntryGate()
        );
    }

    public boolean exitGatePassed(
            EngineeringWorkflow workflow,
            WorkflowTask task
    ) {
        return evaluate(
                workflow,
                task,
                task.getExitGate()
        );
    }

    private boolean evaluate(
            EngineeringWorkflow workflow,
            WorkflowTask task,
            GateDefinition gate
    ) {
        GateType type = gate.getType();

        return switch (type) {
            case NONE -> true;

            case DEPENDENCIES_SUCCEEDED ->
                    task.getDependencyIds()
                            .stream()
                            .map(workflow::requireTask)
                            .allMatch(dependency ->
                                    dependency.getStatus()
                                            == TaskStatus.SUCCEEDED
                            );

            case CONTEXT_KEY_PRESENT ->
                    gate.getRequiredContextKey() != null &&
                            workflow.getContext().contains(
                                    gate.getRequiredContextKey()
                            );

            case HUMAN_APPROVAL ->
                    workflow.getContext().contains(
                            "approval:"
                                    + task.getId()
                                    + ":revision:"
                                    + workflow.getRevision()
                    );
        };
    }
}