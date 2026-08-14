package com.navya.agentic_url_shortener.orchestration.domain;

import lombok.Getter;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Getter
public class EngineeringWorkflow {

    private final UUID id;
    private final ScenarioType scenarioType;
    private final String requirement;
    private final String repositoryPath;

    private final Map<UUID, WorkflowTask> tasks =
            new LinkedHashMap<>();

    private final ExecutionContext context =
            new ExecutionContext();

    private int revision;
    private WorkflowStatus status;

    private final Instant createdAt;
    private Instant startedAt;
    private Instant completedAt;
    private String failureMessage;

    public EngineeringWorkflow(
            UUID id,
            ScenarioType scenarioType,
            String requirement,
            String repositoryPath,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.scenarioType =
                Objects.requireNonNull(scenarioType);

        if (requirement == null || requirement.isBlank()) {
            throw new IllegalArgumentException(
                    "requirement must not be blank"
            );
        }

        this.requirement = requirement.trim();
        this.repositoryPath = repositoryPath;
        this.revision = 1;
        this.status = WorkflowStatus.CREATED;
        this.createdAt = Objects.requireNonNull(createdAt);

        context.put("requirement", this.requirement);
        context.put("scenarioType", scenarioType.name());

        if (repositoryPath != null &&
                !repositoryPath.isBlank()) {
            context.put(
                    "repositoryPath",
                    repositoryPath
            );
        }
    }

    public void addTask(WorkflowTask task) {
        Objects.requireNonNull(task);

        if (status != WorkflowStatus.CREATED) {
            throw new IllegalStateException(
                    "Tasks can only be added before execution"
            );
        }

        if (tasks.putIfAbsent(task.getId(), task) != null) {
            throw new IllegalArgumentException(
                    "Duplicate task ID: " + task.getId()
            );
        }
    }

    public WorkflowTask requireTask(UUID taskId) {
        WorkflowTask task = tasks.get(taskId);

        if (task == null) {
            throw new IllegalArgumentException(
                    "Unknown task ID: " + taskId
            );
        }

        return task;
    }

    public Collection<WorkflowTask> getTasks() {
        return Collections.unmodifiableCollection(
                tasks.values()
        );
    }

    public synchronized void start(Instant startedAt) {
        if (status != WorkflowStatus.CREATED) {
            throw new IllegalStateException(
                    "Only a created workflow can start"
            );
        }

        this.status = WorkflowStatus.RUNNING;
        this.startedAt = Objects.requireNonNull(startedAt);
    }

    public synchronized void complete(Instant completedAt) {
        if (status != WorkflowStatus.RUNNING &&
                status != WorkflowStatus.AWAITING_APPROVAL) {
            throw new IllegalStateException(
                    "Workflow cannot complete from status "
                            + status
            );
        }

        this.status = WorkflowStatus.COMPLETED;
        this.completedAt = Objects.requireNonNull(completedAt);
    }

    public synchronized void fail(
            String failureMessage,
            Instant completedAt
    ) {
        this.status = WorkflowStatus.FAILED;
        this.failureMessage = failureMessage;
        this.completedAt = Objects.requireNonNull(completedAt);
    }

    public synchronized void awaitClarification() {
        this.status = WorkflowStatus.AWAITING_CLARIFICATION;
    }

    public synchronized void awaitApproval() {
        this.status = WorkflowStatus.AWAITING_APPROVAL;
    }

    public synchronized void resume() {
        if (status != WorkflowStatus.AWAITING_CLARIFICATION &&
                status != WorkflowStatus.AWAITING_APPROVAL) {
            throw new IllegalStateException(
                    "Workflow is not waiting"
            );
        }

        this.status = WorkflowStatus.RUNNING;
    }

    public synchronized void incrementRevision() {
        this.revision++;
    }

    public synchronized void safeStop(
            Instant stoppedAt,
            String reason,
            String actor
    ) {
        if (isTerminal()) {
            throw new IllegalStateException(
                    "A terminal workflow cannot be safely stopped"
            );
        }

        tasks.values().forEach(WorkflowTask::cancel);

        context.put("safeStopReason", reason);
        context.put("safeStopActor", actor);

        this.status = WorkflowStatus.SAFE_STOPPED;
        this.completedAt = Objects.requireNonNull(stoppedAt);
    }

    public boolean isWaitingForApproval() {
        return status == WorkflowStatus.AWAITING_APPROVAL;
    }

    public boolean isStopped() {
        return status == WorkflowStatus.SAFE_STOPPED;
    }

    public boolean isTerminal() {
        return status == WorkflowStatus.COMPLETED ||
                status == WorkflowStatus.FAILED ||
                status == WorkflowStatus.SAFE_STOPPED;
    }
}