package com.navya.agentic_url_shortener.orchestration.planner;

import com.navya.agentic_url_shortener.orchestration.domain.ScenarioType;

public interface WorkflowPlanner {

    PlannedWorkflow plan(
            ScenarioType scenarioType,
            String requirement,
            String repositoryPath
    );
}