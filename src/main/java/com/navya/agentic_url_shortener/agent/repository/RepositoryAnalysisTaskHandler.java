package com.navya.agentic_url_shortener.agent.repository;

import com.navya.agentic_url_shortener.artifact.ArtifactReference;
import com.navya.agentic_url_shortener.artifact.ArtifactStore;
import com.navya.agentic_url_shortener.artifact.ArtifactType;
import com.navya.agentic_url_shortener.orchestration.domain.EngineeringWorkflow;
import com.navya.agentic_url_shortener.orchestration.domain.TaskType;
import com.navya.agentic_url_shortener.orchestration.domain.WorkflowTask;
import com.navya.agentic_url_shortener.orchestration.engine.TaskExecutionResult;
import com.navya.agentic_url_shortener.orchestration.engine.WorkflowTaskHandler;
import com.navya.agentic_url_shortener.tool.repository.RepositoryAnalysis;
import com.navya.agentic_url_shortener.tool.repository.RepositoryAnalysisTool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RepositoryAnalysisTaskHandler
        implements WorkflowTaskHandler {

    private final RepositoryAnalysisTool repositoryAnalysisTool;
    private final ArtifactStore artifactStore;

    @Override
    public TaskType supports() {
        return TaskType.REPOSITORY_ANALYSIS;
    }

    @Override
    public TaskExecutionResult execute(
            EngineeringWorkflow workflow,
            WorkflowTask task
    ) {
        RepositoryAnalysis analysis =
                repositoryAnalysisTool.analyze(
                        workflow.getRepositoryPath(),
                        workflow.getRequirement()
                );

        ArtifactReference artifact =
                artifactStore.write(
                        workflow.getId(),
                        workflow.getRevision(),
                        task.getId(),
                        ArtifactType.REPOSITORY_ANALYSIS,
                        "repository-analysis.md",
                        toMarkdown(workflow, analysis)
                );

        Map<String, Object> updates =
                new LinkedHashMap<>();

        updates.put(
                "repositoryAnalysisOutput",
                analysis
        );

        updates.put(
                "repositoryAnalysisArtifact",
                artifact
        );

        return new TaskExecutionResult(updates);
    }

    private String toMarkdown(
            EngineeringWorkflow workflow,
            RepositoryAnalysis analysis
    ) {
        StringBuilder markdown = new StringBuilder();

        markdown.append("# Repository Analysis\n\n");
        markdown.append("## Requirement\n\n");
        markdown.append(workflow.getRequirement())
                .append("\n\n");

        markdown.append("## Repository\n\n");
        markdown.append("- Root: `")
                .append(analysis.getRepositoryRoot())
                .append("`\n");
        markdown.append("- Build system: `")
                .append(analysis.getBuildSystem())
                .append("`\n");
        markdown.append("- Files inspected: ")
                .append(analysis.getInspectedFileCount())
                .append("\n");
        markdown.append("- Truncated: ")
                .append(analysis.isTruncated())
                .append("\n\n");

        appendList(
                markdown,
                "Detected modules",
                analysis.getDetectedModules()
        );

        appendList(
                markdown,
                "Potentially impacted files",
                analysis.getImpactedFiles()
        );

        appendList(
                markdown,
                "Source files",
                analysis.getSourceFiles()
        );

        appendList(
                markdown,
                "Test files",
                analysis.getTestFiles()
        );

        appendList(
                markdown,
                "Database migrations",
                analysis.getMigrationFiles()
        );

        appendList(
                markdown,
                "Configuration files",
                analysis.getConfigurationFiles()
        );

        return markdown.toString();
    }

    private void appendList(
            StringBuilder markdown,
            String heading,
            java.util.List<String> values
    ) {
        markdown.append("## ")
                .append(heading)
                .append("\n\n");

        if (values.isEmpty()) {
            markdown.append("- None detected\n\n");
            return;
        }

        values.forEach(value ->
                markdown.append("- `")
                        .append(value)
                        .append("`\n")
        );

        markdown.append("\n");
    }
}