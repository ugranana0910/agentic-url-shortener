package com.navya.agentic_url_shortener.agent.implementation;

import com.navya.agentic_url_shortener.artifact.ArtifactReference;
import com.navya.agentic_url_shortener.artifact.ArtifactStore;
import com.navya.agentic_url_shortener.artifact.ArtifactType;
import com.navya.agentic_url_shortener.orchestration.domain.EngineeringWorkflow;
import com.navya.agentic_url_shortener.orchestration.domain.TaskType;
import com.navya.agentic_url_shortener.orchestration.domain.WorkflowTask;
import com.navya.agentic_url_shortener.orchestration.engine.TaskExecutionResult;
import com.navya.agentic_url_shortener.orchestration.engine.WorkflowTaskHandler;
import com.navya.agentic_url_shortener.tool.repository.RepositoryAnalysis;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ImplementationTaskHandler
        implements WorkflowTaskHandler {

    private final ArtifactStore artifactStore;

    @Override
    public TaskType supports() {
        return TaskType.IMPLEMENTATION;
    }

    @Override
    public TaskExecutionResult execute(
            EngineeringWorkflow workflow,
            WorkflowTask task
    ) {
        String document = createDocument(workflow);

        ArtifactReference artifact =
                artifactStore.write(
                        workflow.getId(),
                        workflow.getRevision(),
                        task.getId(),
                        ArtifactType.IMPLEMENTATION,
                        "implementation-plan.md",
                        document
                );

        Map<String, Object> output =
                new LinkedHashMap<>();

        output.put("document", document);
        output.put("artifact", artifact);
        output.put(
                "status",
                "REVIEWABLE_IMPLEMENTATION_PLAN"
        );

        return TaskExecutionResult.of(
                "implementationOutput",
                output
        );
    }

    private String createDocument(
            EngineeringWorkflow workflow
    ) {
        List<String> impactedFiles =
                workflow.getContext()
                        .get(
                                "repositoryAnalysisOutput",
                                RepositoryAnalysis.class
                        )
                        .map(
                                RepositoryAnalysis::getImpactedFiles
                        )
                        .orElse(List.of());

        Object acceptanceCriteria =
                workflow.getContext()
                        .get("acceptanceCriteria")
                        .orElse(List.of());

        return """
                # Implementation Plan

                ## Requirement

                %s

                ## Scenario

                %s

                ## Workflow revision

                %d

                ## Acceptance criteria

                %s

                ## Candidate impacted files

                %s

                ## Planned implementation sequence

                1. Confirm current API and persistence behavior.
                2. Make the smallest backward-compatible domain change.
                3. Add or update Flyway migration when persistence changes.
                4. Update service behavior before controller behavior.
                5. Add unit tests for domain and failure behavior.
                6. Add integration coverage for persistence or API changes.
                7. Run the repository's Maven test suite.
                8. Block release readiness when validation fails.

                ## Safety constraints

                - Do not modify files outside the approved repository.
                - Do not bypass Flyway with Hibernate schema updates.
                - Do not remove existing tests to make validation pass.
                - Preserve existing public behavior unless explicitly approved.
                - Keep generated changes reviewable before application.

                ## Current prototype boundary

                This artifact is a requirement-specific implementation plan.
                Source patch application is introduced in the next controlled
                change-execution increment.
                """.formatted(
                workflow.getRequirement(),
                workflow.getScenarioType(),
                workflow.getRevision(),
                acceptanceCriteria,
                impactedFiles
        );
    }
}