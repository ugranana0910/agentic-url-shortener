package com.navya.agentic_url_shortener.agent.validation;

import com.navya.agentic_url_shortener.artifact.ArtifactReference;
import com.navya.agentic_url_shortener.artifact.ArtifactStore;
import com.navya.agentic_url_shortener.artifact.ArtifactType;
import com.navya.agentic_url_shortener.orchestration.domain.EngineeringWorkflow;
import com.navya.agentic_url_shortener.orchestration.domain.TaskType;
import com.navya.agentic_url_shortener.orchestration.domain.WorkflowTask;
import com.navya.agentic_url_shortener.orchestration.engine.TaskExecutionResult;
import com.navya.agentic_url_shortener.orchestration.engine.WorkflowTaskHandler;
import com.navya.agentic_url_shortener.tool.build.BuildExecutionResult;
import com.navya.agentic_url_shortener.tool.build.MavenValidationTool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ValidationTaskHandler
        implements WorkflowTaskHandler {

    private final MavenValidationTool validationTool;
    private final ArtifactStore artifactStore;

    @Override
    public TaskType supports() {
        return TaskType.VALIDATION;
    }

    @Override
    public TaskExecutionResult execute(
            EngineeringWorkflow workflow,
            WorkflowTask task
    ) {
        if (workflow.getRepositoryPath() == null) {
            return validateGreenfieldWithoutRepository(
                    workflow,
                    task
            );
        }

        BuildExecutionResult result =
                validationTool.runTests(
                        workflow.getRepositoryPath()
                );

        ArtifactReference buildLog =
                artifactStore.write(
                        workflow.getId(),
                        workflow.getRevision(),
                        task.getId(),
                        ArtifactType.VALIDATION_REPORT,
                        validationLogName(task),
                        result.getOutput()
                );

        String report = createReport(
                workflow,
                result,
                buildLog
        );

        ArtifactReference reportArtifact =
                artifactStore.write(
                        workflow.getId(),
                        workflow.getRevision(),
                        task.getId(),
                        ArtifactType.VALIDATION_REPORT,
                        validationReportName(task),
                        report
                );

        if (!result.isSuccessful()) {
            throw new IllegalStateException(
                    failureMessage(result)
            );
        }

        Map<String, Object> output =
                new LinkedHashMap<>();

        output.put("passed", true);
        output.put("exitCode", result.getExitCode());
        output.put(
                "durationMillis",
                result.getDuration().toMillis()
        );
        output.put("timedOut", false);
        output.put("buildLogArtifact", buildLog);
        output.put(
                "validationReportArtifact",
                reportArtifact
        );

        return TaskExecutionResult.of(
                "validationOutput",
                output
        );
    }

    private TaskExecutionResult
    validateGreenfieldWithoutRepository(
            EngineeringWorkflow workflow,
            WorkflowTask task
    ) {
        String report = """
                # Validation Report

                - Workflow: `%s`
                - Revision: `%d`
                - Scenario: `GREENFIELD`
                - Decision: `CONDITIONALLY_PASSED`
                - Executable repository validation: `NOT_APPLICABLE`

                The greenfield scenario currently produces design and planning
                artifacts without materializing a repository. Brownfield
                scenarios execute the repository's Maven test suite.
                """.formatted(
                workflow.getId(),
                workflow.getRevision()
        );

        ArtifactReference artifact =
                artifactStore.write(
                        workflow.getId(),
                        workflow.getRevision(),
                        task.getId(),
                        ArtifactType.VALIDATION_REPORT,
                        validationReportName(task),
                        report
                );

        return TaskExecutionResult.of(
                "validationOutput",
                Map.of(
                        "passed",
                        true,
                        "executable",
                        false,
                        "validationReportArtifact",
                        artifact
                )
        );
    }

    private String validationLogName(WorkflowTask task) {
        return "maven-test-attempt-"
                + task.getAttempt()
                + ".log";
    }

    private String validationReportName(
            WorkflowTask task
    ) {
        return "validation-report-attempt-"
                + task.getAttempt()
                + ".md";
    }

    private String createReport(
            EngineeringWorkflow workflow,
            BuildExecutionResult result,
            ArtifactReference buildLog
    ) {
        return """
                # Validation Report

                ## Workflow

                - Workflow ID: `%s`
                - Revision: `%d`
                - Scenario: `%s`

                ## Command

                `%s`

                ## Result

                - Passed: `%s`
                - Exit code: `%d`
                - Timed out: `%s`
                - Duration: `%d ms`
                - Output truncated: `%s`
                - Build log hash: `%s`

                ## Gate decision

                %s
                """.formatted(
                workflow.getId(),
                workflow.getRevision(),
                workflow.getScenarioType(),
                String.join(" ", result.getCommand()),
                result.isSuccessful(),
                result.getExitCode(),
                result.isTimedOut(),
                result.getDuration().toMillis(),
                result.isOutputTruncated(),
                buildLog.getSha256(),
                result.isSuccessful()
                        ? "PASSED - downstream work may continue."
                        : "FAILED - release readiness is blocked."
        );
    }

    private String failureMessage(
            BuildExecutionResult result
    ) {
        if (result.isTimedOut()) {
            return "Maven validation timed out";
        }

        return "Maven validation failed with exit code "
                + result.getExitCode();
    }
}