package com.navya.agentic_url_shortener.unit.orchestration.engine;

import com.navya.agentic_url_shortener.audit.InMemoryAuditJournal;
import com.navya.agentic_url_shortener.audit.WorkflowMetrics;
import com.navya.agentic_url_shortener.orchestration.domain.EngineeringWorkflow;
import com.navya.agentic_url_shortener.orchestration.domain.GateDefinition;
import com.navya.agentic_url_shortener.orchestration.domain.ScenarioType;
import com.navya.agentic_url_shortener.orchestration.domain.TaskType;
import com.navya.agentic_url_shortener.orchestration.domain.WorkflowStatus;
import com.navya.agentic_url_shortener.orchestration.domain.WorkflowTask;
import com.navya.agentic_url_shortener.orchestration.engine.TaskExecutionResult;
import com.navya.agentic_url_shortener.orchestration.engine.WorkflowEngine;
import com.navya.agentic_url_shortener.orchestration.engine.WorkflowGraphValidator;
import com.navya.agentic_url_shortener.orchestration.engine.WorkflowTaskHandler;
import com.navya.agentic_url_shortener.orchestration.engine.WorkflowTaskHandlerRegistry;
import com.navya.agentic_url_shortener.orchestration.gate.WorkflowGateEvaluator;
import com.navya.agentic_url_shortener.orchestration.repository.InMemoryWorkflowRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.assertj.core.api.Assertions.assertThat;

class WorkflowEngineTest {

    private static final Instant NOW =
            Instant.parse("2026-08-13T12:00:00Z");

    private final Clock clock =
            Clock.fixed(NOW, ZoneOffset.UTC);

    private final SimpleMeterRegistry meterRegistry =
            new SimpleMeterRegistry();

    private final InMemoryAuditJournal auditJournal =
            new InMemoryAuditJournal(clock);

    private final WorkflowMetrics workflowMetrics =
            new WorkflowMetrics(meterRegistry);

    @Test
    void executesParallelBranchesBeforeJoin() {
        ConcurrentLinkedQueue<String> events =
                new ConcurrentLinkedQueue<>();

        WorkflowTaskHandler implementationHandler =
                handler(
                        TaskType.IMPLEMENTATION,
                        "implementation",
                        events
                );

        WorkflowTaskHandler testPlanningHandler =
                handler(
                        TaskType.TEST_PLANNING,
                        "test-planning",
                        events
                );

        WorkflowTaskHandler validationHandler =
                new WorkflowTaskHandler() {

                    @Override
                    public TaskType supports() {
                        return TaskType.VALIDATION;
                    }

                    @Override
                    public TaskExecutionResult execute(
                            EngineeringWorkflow workflow,
                            WorkflowTask task
                    ) {
                        assertThat(
                                workflow.getContext()
                                        .contains("implementation")
                        ).isTrue();

                        assertThat(
                                workflow.getContext()
                                        .contains("test-planning")
                        ).isTrue();

                        events.add("validation");

                        return TaskExecutionResult.of(
                                "validated",
                                true
                        );
                    }
                };

        WorkflowEngine engine = createEngine(
                List.of(
                        implementationHandler,
                        testPlanningHandler,
                        validationHandler
                )
        );

        EngineeringWorkflow workflow =
                new EngineeringWorkflow(
                        UUID.randomUUID(),
                        ScenarioType.BROWNFIELD,
                        "Add redirect analytics",
                        "./repository",
                        NOW
                );

        WorkflowTask implementation = task(
                "Implementation",
                TaskType.IMPLEMENTATION,
                Set.of()
        );

        WorkflowTask testPlanning = task(
                "Test planning",
                TaskType.TEST_PLANNING,
                Set.of()
        );

        WorkflowTask validation = task(
                "Validation",
                TaskType.VALIDATION,
                Set.of(
                        implementation.getId(),
                        testPlanning.getId()
                )
        );

        workflow.addTask(implementation);
        workflow.addTask(testPlanning);
        workflow.addTask(validation);

        engine.execute(workflow);

        assertThat(workflow.getStatus())
                .isEqualTo(WorkflowStatus.COMPLETED);

        assertThat(events)
                .containsExactlyInAnyOrder(
                        "implementation",
                        "test-planning",
                        "validation"
                );

        assertThat(events.peek())
                .isNotEqualTo("validation");
    }

    @Test
    void retriesFailedTaskWithinBound() {
        int[] attempts = {0};

        WorkflowTaskHandler implementationHandler =
                new WorkflowTaskHandler() {

                    @Override
                    public TaskType supports() {
                        return TaskType.IMPLEMENTATION;
                    }

                    @Override
                    public TaskExecutionResult execute(
                            EngineeringWorkflow workflow,
                            WorkflowTask task
                    ) {
                        attempts[0]++;

                        if (attempts[0] == 1) {
                            throw new IllegalStateException(
                                    "Controlled first-attempt failure"
                            );
                        }

                        return TaskExecutionResult.of(
                                "implementation",
                                "completed"
                        );
                    }
                };

        WorkflowEngine engine = createEngine(
                List.of(implementationHandler)
        );

        EngineeringWorkflow workflow =
                new EngineeringWorkflow(
                        UUID.randomUUID(),
                        ScenarioType.BROWNFIELD,
                        "Implement a change",
                        "./repository",
                        NOW
                );

        WorkflowTask implementationTask = task(
                "Implementation",
                TaskType.IMPLEMENTATION,
                Set.of()
        );

        workflow.addTask(implementationTask);

        engine.execute(workflow);

        assertThat(workflow.getStatus())
                .isEqualTo(WorkflowStatus.COMPLETED);

        assertThat(implementationTask.getAttempt())
                .isEqualTo(2);

        assertThat(attempts[0])
                .isEqualTo(2);
    }

    private WorkflowEngine createEngine(
            List<WorkflowTaskHandler> handlers
    ) {
        return new WorkflowEngine(
                new WorkflowGraphValidator(),
                new WorkflowGateEvaluator(),
                new WorkflowTaskHandlerRegistry(handlers),
                new InMemoryWorkflowRepository(),
                clock,
                auditJournal,
                workflowMetrics
        );
    }

    private WorkflowTaskHandler handler(
            TaskType type,
            String contextKey,
            ConcurrentLinkedQueue<String> events
    ) {
        return new WorkflowTaskHandler() {

            @Override
            public TaskType supports() {
                return type;
            }

            @Override
            public TaskExecutionResult execute(
                    EngineeringWorkflow workflow,
                    WorkflowTask task
            ) {
                events.add(contextKey);

                return TaskExecutionResult.of(
                        contextKey,
                        "completed"
                );
            }
        };
    }

    private WorkflowTask task(
            String name,
            TaskType type,
            Set<UUID> dependencies
    ) {
        return new WorkflowTask(
                UUID.randomUUID(),
                name,
                type,
                dependencies,
                GateDefinition.dependenciesSucceeded(),
                GateDefinition.none(),
                2
        );
    }
}