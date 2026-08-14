package com.navya.agentic_url_shortener.orchestration.engine;

import com.navya.agentic_url_shortener.orchestration.domain.EngineeringWorkflow;
import com.navya.agentic_url_shortener.orchestration.domain.TaskStatus;
import com.navya.agentic_url_shortener.orchestration.domain.WorkflowStatus;
import com.navya.agentic_url_shortener.orchestration.domain.WorkflowTask;
import com.navya.agentic_url_shortener.orchestration.gate.WorkflowGateEvaluator;
import com.navya.agentic_url_shortener.orchestration.repository.WorkflowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class WorkflowEngine {

    private final WorkflowGraphValidator graphValidator;
    private final WorkflowGateEvaluator gateEvaluator;
    private final WorkflowTaskHandlerRegistry handlerRegistry;
    private final WorkflowRepository workflowRepository;
    private final Clock clock;

    private final Executor executor =
            Executors.newVirtualThreadPerTaskExecutor();

    public EngineeringWorkflow execute(
            EngineeringWorkflow workflow
    ) {
        graphValidator.validate(workflow);

        workflowRepository.save(workflow);
        workflow.start(clock.instant());

        return continueExecution(workflow);
    }

    public EngineeringWorkflow resume(
            EngineeringWorkflow workflow
    ) {
        if (workflow.getStatus()
                != WorkflowStatus.RUNNING) {
            throw new IllegalStateException(
                    "Workflow must be RUNNING before execution resumes"
            );
        }

        return continueExecution(workflow);
    }

    private EngineeringWorkflow continueExecution(
            EngineeringWorkflow workflow
    ) {
        while (workflow.getStatus()
                == WorkflowStatus.RUNNING) {

            List<WorkflowTask> runnable =
                    findRunnableTasks(workflow);

            if (runnable.isEmpty()) {
                finishOrBlock(workflow);
                break;
            }

            executeParallelWave(workflow, runnable);

            if (hasFinalFailure(workflow)) {
                workflow.fail(
                        "One or more workflow tasks failed",
                        clock.instant()
                );
            }

            workflowRepository.save(workflow);
        }

        workflowRepository.save(workflow);

        return workflow;
    }
    private List<WorkflowTask> findRunnableTasks(
            EngineeringWorkflow workflow
    ) {
        return workflow.getTasks()
                .stream()
                .filter(task ->
                        task.getStatus()
                                == TaskStatus.PENDING
                )
                .filter(task ->
                        gateEvaluator.entryGatePassed(
                                workflow,
                                task
                        )
                )
                .toList();
    }

    private void executeParallelWave(
            EngineeringWorkflow workflow,
            List<WorkflowTask> runnable
    ) {
        List<CompletableFuture<Void>> futures =
                new ArrayList<>();

        for (WorkflowTask task : runnable) {
            CompletableFuture<Void> future =
                    CompletableFuture.runAsync(
                            () -> executeTask(
                                    workflow,
                                    task
                            ),
                            executor
                    );

            futures.add(future);
        }

        CompletableFuture.allOf(
                futures.toArray(
                        CompletableFuture[]::new
                )
        ).join();
    }

    private void executeTask(
            EngineeringWorkflow workflow,
            WorkflowTask task
    ) {
        task.start(clock.instant());

        try {
            WorkflowTaskHandler handler =
                    handlerRegistry.require(
                            task.getType()
                    );

            TaskExecutionResult result =
                    handler.execute(workflow, task);

            applyContextUpdates(
                    workflow,
                    result.getContextUpdates()
            );

            if (!gateEvaluator.exitGatePassed(
                    workflow,
                    task
            )) {
                throw new IllegalStateException(
                        "Task exit gate did not pass"
                );
            }

            task.succeed(clock.instant());
        } catch (RuntimeException exception) {
            task.fail(
                    safeMessage(exception),
                    clock.instant()
            );

            if (task.canRetry()) {
                task.prepareRetry();
            }
        }
    }

    private void applyContextUpdates(
            EngineeringWorkflow workflow,
            Map<String, Object> updates
    ) {
        updates.forEach(
                workflow.getContext()::put
        );
    }

    private boolean hasFinalFailure(
            EngineeringWorkflow workflow
    ) {
        return workflow.getTasks()
                .stream()
                .anyMatch(task ->
                        task.getStatus()
                                == TaskStatus.FAILED
                );
    }

    private void finishOrBlock(
            EngineeringWorkflow workflow
    ) {
        boolean allSucceeded = workflow.getTasks()
                .stream()
                .allMatch(task ->
                        task.getStatus()
                                == TaskStatus.SUCCEEDED
                );

        if (allSucceeded) {
            workflow.complete(clock.instant());
            return;
        }

        boolean approvalRequired =
                workflow.getTasks()
                        .stream()
                        .filter(task ->
                                task.getStatus()
                                        == TaskStatus.PENDING
                        )
                        .anyMatch(task ->
                                task.getEntryGate().getType()
                                        == com.navya.agentic_url_shortener
                                        .orchestration.domain.GateType
                                        .HUMAN_APPROVAL
                        );

        if (approvalRequired) {
            workflow.awaitApproval();
            return;
        }

        workflow.fail(
                "Workflow cannot make further progress",
                clock.instant()
        );
    }

    private String safeMessage(RuntimeException exception) {
        if (exception.getMessage() == null ||
                exception.getMessage().isBlank()) {
            return exception.getClass().getSimpleName();
        }

        return exception.getMessage();
    }
}