package com.navya.agentic_url_shortener.orchestration.repository;

import com.navya.agentic_url_shortener.orchestration.domain.EngineeringWorkflow;

import java.util.Optional;
import java.util.UUID;

public interface WorkflowRepository {

    EngineeringWorkflow save(
            EngineeringWorkflow workflow
    );

    Optional<EngineeringWorkflow> findById(UUID id);
}