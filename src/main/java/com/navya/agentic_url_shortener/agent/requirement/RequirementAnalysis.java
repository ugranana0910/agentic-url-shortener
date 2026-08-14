package com.navya.agentic_url_shortener.agent.requirement;

import lombok.Value;

import java.util.List;

@Value
public class RequirementAnalysis {

    String normalizedRequirement;
    List<String> acceptanceCriteria;
    List<String> ambiguities;
    List<String> assumptions;
    RiskLevel riskLevel;
    boolean documentationOnly;

    public boolean requiresClarification() {
        return !ambiguities.isEmpty();
    }
}