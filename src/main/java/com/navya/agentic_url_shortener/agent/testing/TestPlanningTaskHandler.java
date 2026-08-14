package com.navya.agentic_url_shortener.agent.testing;

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
public class TestPlanningTaskHandler
        implements WorkflowTaskHandler {

    private final ArtifactStore artifactStore;

    @Override
    public TaskType supports() {
        return TaskType.TEST_PLANNING;
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
                        ArtifactType.TEST_PLAN,
                        "test-plan.md",
                        document
                );

        Map<String, Object> output =
                new LinkedHashMap<>();

        output.put("document", document);
        output.put("artifact", artifact);

        return TaskExecutionResult.of(
                "testPlanOutput",
                output
        );
    }

    private String createDocument(
            EngineeringWorkflow workflow
    ) {
        Object acceptanceCriteria =
                workflow.getContext()
                        .get("acceptanceCriteria")
                        .orElse(java.util.List.of());

        return """
                # Test Plan

                ## Requirement

                %s

                ## Acceptance criteria

                %s

                ## Unit tests

                - Test the primary successful behavior.
                - Test invalid and boundary inputs.
                - Test state transitions and domain invariants.
                - Test retry and failure behavior where applicable.

                ## Integration tests

                - Verify persistence mappings and Flyway schema compatibility.
                - Verify API status codes, headers, and Problem Details.
                - Verify duplicate and concurrent request behavior.
                - Verify backward compatibility of existing endpoints.

                ## Regression validation

                - Run the full Maven test suite.
                - Do not skip existing tests.
                - Treat timeout or non-zero exit code as a failed gate.

                ## Exit criteria

                - Maven exits successfully.
                - No test failures or errors are reported.
                - Validation evidence is retained as a workflow artifact.
                """.formatted(
                workflow.getRequirement(),
                acceptanceCriteria
        );
    }
}