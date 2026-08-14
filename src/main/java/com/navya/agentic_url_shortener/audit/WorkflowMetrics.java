package com.navya.agentic_url_shortener.audit;

import com.navya.agentic_url_shortener.orchestration.domain.EngineeringWorkflow;
import com.navya.agentic_url_shortener.orchestration.domain.WorkflowTask;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class WorkflowMetrics {

    private final MeterRegistry meterRegistry;

    public void workflowStarted(
            EngineeringWorkflow workflow
    ) {
        meterRegistry.counter(
                "agentic.workflows",
                "event",
                "started",
                "scenario",
                workflow.getScenarioType().name()
        ).increment();
    }

    public void workflowCompleted(
            EngineeringWorkflow workflow
    ) {
        meterRegistry.counter(
                "agentic.workflows",
                "event",
                "completed",
                "scenario",
                workflow.getScenarioType().name()
        ).increment();

        recordWorkflowDuration(workflow, "completed");
    }

    public void workflowFailed(
            EngineeringWorkflow workflow
    ) {
        meterRegistry.counter(
                "agentic.workflows",
                "event",
                "failed",
                "scenario",
                workflow.getScenarioType().name()
        ).increment();

        recordWorkflowDuration(workflow, "failed");
    }

    public void clarificationRequired(
            EngineeringWorkflow workflow
    ) {
        meterRegistry.counter(
                "agentic.clarifications",
                "scenario",
                workflow.getScenarioType().name()
        ).increment();
    }

    public void taskSucceeded(WorkflowTask task) {
        meterRegistry.counter(
                "agentic.tasks",
                "type",
                task.getType().name(),
                "status",
                "succeeded"
        ).increment();
    }

    public void taskFailed(WorkflowTask task) {
        meterRegistry.counter(
                "agentic.tasks",
                "type",
                task.getType().name(),
                "status",
                "failed"
        ).increment();
    }

    public void taskRetried(WorkflowTask task) {
        meterRegistry.counter(
                "agentic.retries",
                "type",
                task.getType().name()
        ).increment();
    }

    public void approvalGranted() {
        meterRegistry.counter(
                "agentic.approvals",
                "decision",
                "granted"
        ).increment();
    }

    public void safeStopped() {
        meterRegistry.counter(
                "agentic.safe.stops"
        ).increment();
    }

    private void recordWorkflowDuration(
            EngineeringWorkflow workflow,
            String outcome
    ) {
        if (workflow.getStartedAt() == null ||
                workflow.getCompletedAt() == null) {
            return;
        }

        Timer.builder("agentic.workflow.duration")
                .tag(
                        "scenario",
                        workflow.getScenarioType().name()
                )
                .tag("outcome", outcome)
                .register(meterRegistry)
                .record(
                        Duration.between(
                                workflow.getStartedAt(),
                                workflow.getCompletedAt()
                        )
                );
    }
}