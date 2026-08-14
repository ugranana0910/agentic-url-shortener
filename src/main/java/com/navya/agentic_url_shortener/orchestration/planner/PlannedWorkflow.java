package com.navya.agentic_url_shortener.orchestration.planner;

import com.navya.agentic_url_shortener.agent.requirement.RequirementAnalysis;
import com.navya.agentic_url_shortener.orchestration.domain.EngineeringWorkflow;
import lombok.Value;

@Value
public class PlannedWorkflow {

    EngineeringWorkflow workflow;
    RequirementAnalysis analysis;
}