package com.navya.agentic_url_shortener.agent.requirement;

import com.navya.agentic_url_shortener.orchestration.domain.ScenarioType;

public interface RequirementAnalyzer {

    RequirementAnalysis analyze(
            String requirement,
            ScenarioType scenarioType
    );
}