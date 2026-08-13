package com.navya.agentic_url_shortener.orchestration.repository;

import com.navya.agentic_url_shortener.orchestration.domain.EngineeringWorkflow;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InMemoryWorkflowRepository
        implements WorkflowRepository {

    private final ConcurrentMap<UUID, EngineeringWorkflow>
            workflows = new ConcurrentHashMap<>();

    @Override
    public EngineeringWorkflow save(
            EngineeringWorkflow workflow
    ) {
        workflows.put(workflow.getId(), workflow);
        return workflow;
    }

    @Override
    public Optional<EngineeringWorkflow> findById(UUID id) {
        return Optional.ofNullable(workflows.get(id));
    }
}