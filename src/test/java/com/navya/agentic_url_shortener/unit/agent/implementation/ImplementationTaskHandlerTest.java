package com.navya.agentic_url_shortener.unit.agent.implementation;

import com.navya.agentic_url_shortener.agent.implementation.ImplementationTaskHandler;
import com.navya.agentic_url_shortener.artifact.FileSystemArtifactStore;
import com.navya.agentic_url_shortener.config.AgenticProperties;
import com.navya.agentic_url_shortener.orchestration.domain.EngineeringWorkflow;
import com.navya.agentic_url_shortener.orchestration.domain.GateDefinition;
import com.navya.agentic_url_shortener.orchestration.domain.ScenarioType;
import com.navya.agentic_url_shortener.orchestration.domain.TaskType;
import com.navya.agentic_url_shortener.orchestration.domain.WorkflowTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ImplementationTaskHandlerTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void createsVersionedImplementationPlan()
            throws Exception {
        AgenticProperties properties =
                new AgenticProperties();

        properties.getWorkspace()
                .setRoot(temporaryDirectory);

        FileSystemArtifactStore store =
                new FileSystemArtifactStore(
                        properties,
                        Clock.fixed(
                                Instant.parse(
                                        "2026-08-13T12:00:00Z"
                                ),
                                ZoneOffset.UTC
                        )
                );

        ImplementationTaskHandler handler =
                new ImplementationTaskHandler(store);

        EngineeringWorkflow workflow =
                new EngineeringWorkflow(
                        UUID.randomUUID(),
                        ScenarioType.BROWNFIELD,
                        "Add redirect analytics",
                        ".",
                        Instant.parse(
                                "2026-08-13T12:00:00Z"
                        )
                );

        workflow.getContext().put(
                "acceptanceCriteria",
                java.util.List.of(
                        "Analytics must not change redirects"
                )
        );

        WorkflowTask task = new WorkflowTask(
                UUID.randomUUID(),
                "Implementation",
                TaskType.IMPLEMENTATION,
                Set.of(),
                GateDefinition.dependenciesSucceeded(),
                GateDefinition.contextKeyPresent(
                        "implementationOutput"
                ),
                2
        );

        var result = handler.execute(workflow, task);

        assertThat(result.getContextUpdates())
                .containsKey("implementationOutput");

        var artifacts =
                store.findByWorkflowId(workflow.getId());

        assertThat(artifacts).hasSize(1);

        Path file = temporaryDirectory.resolve(
                artifacts.getFirst().getRelativePath()
        );

        assertThat(Files.readString(file))
                .contains(
                        "Add redirect analytics",
                        "Planned implementation sequence",
                        "Safety constraints"
                );
    }
}