package com.navya.agentic_url_shortener.agent.requirement;

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
public class RequirementAnalysisTaskHandler
        implements WorkflowTaskHandler {

    private final RequirementAnalyzer analyzer;

    @Override
    public TaskType supports() {
        return TaskType.REQUIREMENT_ANALYSIS;
    }

    @Override
    public TaskExecutionResult execute(
            EngineeringWorkflow workflow,
            WorkflowTask task
    ) {
        RequirementAnalysis analysis =
                analyzer.analyze(
                        workflow.getRequirement(),
                        workflow.getScenarioType()
                );

        Map<String, Object> output =
                new LinkedHashMap<>();

        output.put(
                "normalizedRequirement",
                analysis.getNormalizedRequirement()
        );

        output.put(
                "acceptanceCriteria",
                analysis.getAcceptanceCriteria()
        );

        output.put(
                "ambiguities",
                analysis.getAmbiguities()
        );

        output.put(
                "assumptions",
                analysis.getAssumptions()
        );

        output.put(
                "riskLevel",
                analysis.getRiskLevel().name()
        );

        output.put(
                "requirementAnalysis",
                analysis
        );

        if (analysis.requiresClarification()) {
            workflow.awaitClarification();
        }

        return new TaskExecutionResult(output);
    }
}