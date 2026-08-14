package com.navya.agentic_url_shortener.unit.policy;

import com.navya.agentic_url_shortener.artifact.ArtifactReference;
import com.navya.agentic_url_shortener.artifact.ArtifactType;
import com.navya.agentic_url_shortener.governance.PolicyViolationException;
import com.navya.agentic_url_shortener.orchestration.domain.EngineeringWorkflow;
import com.navya.agentic_url_shortener.orchestration.domain.GateDefinition;
import com.navya.agentic_url_shortener.orchestration.domain.ScenarioType;
import com.navya.agentic_url_shortener.orchestration.domain.TaskType;
import com.navya.agentic_url_shortener.orchestration.domain.WorkflowTask;
import com.navya.agentic_url_shortener.policy.ReleasePolicyEngine;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReleasePolicyEngineTest {

    private static final Instant NOW =
            Instant.parse("2026-08-13T12:00:00Z");

    private final ReleasePolicyEngine engine =
            new ReleasePolicyEngine();

    @Test
    void passesWhenValidationAndArtifactsExist() {
        EngineeringWorkflow workflow =
                workflowWithSuccessfulValidation();

        List<ArtifactReference> artifacts =
                List.of(
                        artifact(
                                workflow,
                                ArtifactType.ARCHITECTURE
                        ),
                        artifact(
                                workflow,
                                ArtifactType.VALIDATION_REPORT
                        )
                );

        assertThatCode(
                () -> engine.enforce(
                        workflow,
                        artifacts
                )
        ).doesNotThrowAnyException();
    }

    @Test
    void rejectsMissingValidationEvidence() {
        EngineeringWorkflow workflow =
                workflowWithSuccessfulValidation();

        List<ArtifactReference> artifacts =
                List.of(
                        artifact(
                                workflow,
                                ArtifactType.ARCHITECTURE
                        )
                );

        assertThatThrownBy(
                () -> engine.enforce(
                        workflow,
                        artifacts
                )
        )
                .isInstanceOf(
                        PolicyViolationException.class
                )
                .hasMessageContaining(
                        "POL-VALIDATION-EVIDENCE"
                );
    }

    private EngineeringWorkflow
    workflowWithSuccessfulValidation() {
        EngineeringWorkflow workflow =
                new EngineeringWorkflow(
                        UUID.randomUUID(),
                        ScenarioType.BROWNFIELD,
                        "Add analytics",
                        ".",
                        NOW
                );

        WorkflowTask validation =
                new WorkflowTask(
                        UUID.randomUUID(),
                        "Validation",
                        TaskType.VALIDATION,
                        Set.of(),
                        GateDefinition.dependenciesSucceeded(),
                        GateDefinition.none(),
                        1
                );

        workflow.addTask(validation);

        validation.start(NOW);
        validation.succeed(NOW.plusSeconds(1));

        return workflow;
    }

    private ArtifactReference artifact(
            EngineeringWorkflow workflow,
            ArtifactType type
    ) {
        return new ArtifactReference(
                UUID.randomUUID(),
                workflow.getId(),
                workflow.getRevision(),
                UUID.randomUUID(),
                type,
                type.name().toLowerCase() + ".md",
                "path",
                "a".repeat(64),
                100,
                NOW
        );
    }
}