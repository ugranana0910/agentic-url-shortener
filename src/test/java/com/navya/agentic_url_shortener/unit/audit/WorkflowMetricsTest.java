package com.navya.agentic_url_shortener.unit.audit;

import com.navya.agentic_url_shortener.audit.WorkflowMetrics;
import com.navya.agentic_url_shortener.orchestration.domain.EngineeringWorkflow;
import com.navya.agentic_url_shortener.orchestration.domain.ScenarioType;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WorkflowMetricsTest {

    @Test
    void recordsWorkflowStart() {
        SimpleMeterRegistry registry =
                new SimpleMeterRegistry();

        WorkflowMetrics metrics =
                new WorkflowMetrics(registry);

        EngineeringWorkflow workflow =
                new EngineeringWorkflow(
                        UUID.randomUUID(),
                        ScenarioType.BROWNFIELD,
                        "Add redirect analytics",
                        ".",
                        Instant.parse(
                                "2026-08-13T12:00:00Z"
                        )
                );

        metrics.workflowStarted(workflow);

        assertThat(
                registry.get("agentic.workflows")
                        .tag("event", "started")
                        .tag("scenario", "BROWNFIELD")
                        .counter()
                        .count()
        ).isEqualTo(1.0);
    }
}