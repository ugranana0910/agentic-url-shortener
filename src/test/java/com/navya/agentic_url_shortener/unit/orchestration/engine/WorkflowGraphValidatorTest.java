package com.navya.agentic_url_shortener.unit.orchestration.engine;

import com.navya.agentic_url_shortener.orchestration.domain.EngineeringWorkflow;
import com.navya.agentic_url_shortener.orchestration.domain.GateDefinition;
import com.navya.agentic_url_shortener.orchestration.domain.ScenarioType;
import com.navya.agentic_url_shortener.orchestration.domain.TaskType;
import com.navya.agentic_url_shortener.orchestration.domain.WorkflowTask;
import com.navya.agentic_url_shortener.orchestration.engine.WorkflowGraphValidator;
import com.navya.agentic_url_shortener.orchestration.exception.InvalidWorkflowGraphException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkflowGraphValidatorTest {

    private final WorkflowGraphValidator validator =
            new WorkflowGraphValidator();

    @Test
    void acceptsValidDependencyGraph() {
        EngineeringWorkflow workflow = workflow();

        WorkflowTask analyze = task(
                UUID.randomUUID(),
                Set.of()
        );

        WorkflowTask implement = task(
                UUID.randomUUID(),
                Set.of(analyze.getId())
        );

        workflow.addTask(analyze);
        workflow.addTask(implement);

        assertThatCode(() -> validator.validate(workflow))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsMissingDependency() {
        EngineeringWorkflow workflow = workflow();

        workflow.addTask(
                task(
                        UUID.randomUUID(),
                        Set.of(UUID.randomUUID())
                )
        );

        assertThatThrownBy(
                () -> validator.validate(workflow)
        )
                .isInstanceOf(
                        InvalidWorkflowGraphException.class
                )
                .hasMessageContaining(
                        "references missing dependency"
                );
    }

    @Test
    void rejectsCycle() {
        EngineeringWorkflow workflow = workflow();

        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();

        workflow.addTask(
                task(firstId, Set.of(secondId))
        );

        workflow.addTask(
                task(secondId, Set.of(firstId))
        );

        assertThatThrownBy(
                () -> validator.validate(workflow)
        )
                .isInstanceOf(
                        InvalidWorkflowGraphException.class
                )
                .hasMessage(
                        "Workflow dependency graph contains a cycle"
                );
    }

    private EngineeringWorkflow workflow() {
        return new EngineeringWorkflow(
                UUID.randomUUID(),
                ScenarioType.BROWNFIELD,
                "Add analytics",
                "./repository",
                Instant.parse("2026-08-13T12:00:00Z")
        );
    }

    private WorkflowTask task(
            UUID id,
            Set<UUID> dependencies
    ) {
        return new WorkflowTask(
                id,
                "Test task",
                TaskType.IMPLEMENTATION,
                dependencies,
                GateDefinition.dependenciesSucceeded(),
                GateDefinition.none(),
                2
        );
    }
}