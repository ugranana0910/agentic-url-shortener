package com.navya.agentic_url_shortener.unit.orchestration.gate;

import com.navya.agentic_url_shortener.orchestration.domain.EngineeringWorkflow;
import com.navya.agentic_url_shortener.orchestration.domain.GateDefinition;
import com.navya.agentic_url_shortener.orchestration.domain.ScenarioType;
import com.navya.agentic_url_shortener.orchestration.domain.TaskType;
import com.navya.agentic_url_shortener.orchestration.domain.WorkflowTask;
import com.navya.agentic_url_shortener.orchestration.gate.WorkflowGateEvaluator;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WorkflowGateEvaluatorTest {

    @Test
    void approvalIsBoundToCurrentRevision() {
        EngineeringWorkflow workflow =
                new EngineeringWorkflow(
                        UUID.randomUUID(),
                        ScenarioType.GREENFIELD,
                        "Create an API",
                        null,
                        Instant.parse(
                                "2026-08-13T12:00:00Z"
                        )
                );

        WorkflowTask releaseTask =
                new WorkflowTask(
                        UUID.randomUUID(),
                        "Release readiness",
                        TaskType.RELEASE_READINESS,
                        Set.of(),
                        GateDefinition.humanApproval(),
                        GateDefinition.none(),
                        1
                );

        workflow.addTask(releaseTask);

        WorkflowGateEvaluator evaluator =
                new WorkflowGateEvaluator();

        assertThat(
                evaluator.entryGatePassed(
                        workflow,
                        releaseTask
                )
        ).isFalse();

        workflow.getContext().put(
                "approval:"
                        + releaseTask.getId()
                        + ":revision:1",
                "approved"
        );

        assertThat(
                evaluator.entryGatePassed(
                        workflow,
                        releaseTask
                )
        ).isTrue();

        workflow.incrementRevision();

        assertThat(
                evaluator.entryGatePassed(
                        workflow,
                        releaseTask
                )
        ).isFalse();
    }
}