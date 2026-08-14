package com.navya.agentic_url_shortener.agent.architecture;

import com.navya.agentic_url_shortener.artifact.ArtifactReference;
import com.navya.agentic_url_shortener.artifact.ArtifactStore;
import com.navya.agentic_url_shortener.artifact.ArtifactType;
import com.navya.agentic_url_shortener.orchestration.domain.EngineeringWorkflow;
import com.navya.agentic_url_shortener.orchestration.domain.TaskType;
import com.navya.agentic_url_shortener.orchestration.domain.WorkflowTask;
import com.navya.agentic_url_shortener.orchestration.engine.TaskExecutionResult;
import com.navya.agentic_url_shortener.orchestration.engine.WorkflowTaskHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ArchitectureTaskHandler
        implements WorkflowTaskHandler {

    private final ArtifactStore artifactStore;

    @Override
    public TaskType supports() {
        return TaskType.ARCHITECTURE_DESIGN;
    }

    @Override
    public TaskExecutionResult execute(
            EngineeringWorkflow workflow,
            WorkflowTask task
    ) {
        String architecture =
                createArchitectureDocument(workflow);

        ArtifactReference artifact =
                artifactStore.write(
                        workflow.getId(),
                        workflow.getRevision(),
                        task.getId(),
                        ArtifactType.ARCHITECTURE,
                        "architecture.md",
                        architecture
                );

        Map<String, Object> output =
                new LinkedHashMap<>();

        output.put(
                "architectureDocument",
                architecture
        );

        output.put(
                "architectureArtifact",
                artifact
        );

        return TaskExecutionResult.of(
                "architectureOutput",
                output
        );
    }

    private String createArchitectureDocument(
            EngineeringWorkflow workflow
    ) {
        Object acceptanceCriteria =
                workflow.getContext()
                        .get("acceptanceCriteria")
                        .orElse(java.util.List.of());

        Object impactedFiles =
                workflow.getContext()
                        .get("repositoryAnalysisOutput")
                        .map(value ->
                                ((com.navya.agentic_url_shortener
                                        .tool.repository.RepositoryAnalysis)
                                        value).getImpactedFiles()
                        )
                        .orElse(java.util.List.of());

        return """
                # Architecture Decision

                ## Requirement

                %s

                ## Scenario

                %s

                ## Workflow revision

                %d

                ## Acceptance criteria

                %s

                ## Potentially impacted files

                %s

                ## Proposed approach

                - Preserve existing public API compatibility.
                - Keep validation and domain behavior in dedicated services.
                - Apply persistence changes through versioned Flyway migrations.
                - Add unit and integration tests before release readiness.
                - Require executable validation before approval.

                ## Risks and controls

                - Regression risk: controlled through automated tests.
                - Schema risk: controlled through Flyway and Hibernate validation.
                - Change-scope risk: controlled through approved repository boundaries.
                - Release risk: controlled through validation and approval gates.

                ## Trade-offs

                This deterministic architecture artifact is reproducible and suitable
                for CI. A model-backed architecture agent can replace the generator
                while preserving the same workflow and artifact contracts.
                """.formatted(
                workflow.getRequirement(),
                workflow.getScenarioType(),
                workflow.getRevision(),
                acceptanceCriteria,
                impactedFiles
        );
    }
}