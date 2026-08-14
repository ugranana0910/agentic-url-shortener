package com.navya.agentic_url_shortener.orchestration.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkflowTask {

    private UUID id;
    private String name;
    private TaskType type;
    private TaskStatus status;

    private Set<UUID> dependencyIds =
            new LinkedHashSet<>();

    private GateDefinition entryGate;
    private GateDefinition exitGate;

    private int attempt;
    private int maxAttempts;

    private Instant startedAt;
    private Instant completedAt;
    private String failureMessage;

    public WorkflowTask(
            UUID id,
            String name,
            TaskType type,
            Set<UUID> dependencyIds,
            GateDefinition entryGate,
            GateDefinition exitGate,
            int maxAttempts
    ) {
        this.id = Objects.requireNonNull(id);
        this.name = requireText(name, "name");
        this.type = Objects.requireNonNull(type);
        this.status = TaskStatus.PENDING;

        this.dependencyIds = dependencyIds == null
                ? new LinkedHashSet<>()
                : new LinkedHashSet<>(dependencyIds);

        this.entryGate = entryGate == null
                ? GateDefinition.dependenciesSucceeded()
                : entryGate;

        this.exitGate = exitGate == null
                ? GateDefinition.none()
                : exitGate;

        if (maxAttempts < 1) {
            throw new IllegalArgumentException(
                    "maxAttempts must be at least 1"
            );
        }

        this.maxAttempts = maxAttempts;
    }

    public synchronized void start(Instant startedAt) {
        if (status != TaskStatus.PENDING) {
            throw new IllegalStateException(
                    "Only a pending task can be started"
            );
        }

        this.status = TaskStatus.RUNNING;
        this.startedAt = Objects.requireNonNull(startedAt);
        this.attempt++;
        this.failureMessage = null;
    }

    public synchronized void succeed(Instant completedAt) {
        if (status != TaskStatus.RUNNING) {
            throw new IllegalStateException(
                    "Only a running task can succeed"
            );
        }

        this.status = TaskStatus.SUCCEEDED;
        this.completedAt = Objects.requireNonNull(completedAt);
    }

    public synchronized void fail(
            String message,
            Instant completedAt
    ) {
        if (status != TaskStatus.RUNNING) {
            throw new IllegalStateException(
                    "Only a running task can fail"
            );
        }

        this.status = TaskStatus.FAILED;
        this.failureMessage = requireText(
                message,
                "failure message"
        );
        this.completedAt = Objects.requireNonNull(completedAt);
    }

    public synchronized void prepareRetry() {
        if (status != TaskStatus.FAILED) {
            throw new IllegalStateException(
                    "Only a failed task can be retried"
            );
        }

        if (!canRetry()) {
            throw new IllegalStateException(
                    "Task retry limit has been reached"
            );
        }

        this.status = TaskStatus.PENDING;
        this.startedAt = null;
        this.completedAt = null;
        this.failureMessage = null;
    }

    public synchronized void block(String reason) {
        if (status != TaskStatus.PENDING) {
            return;
        }

        this.status = TaskStatus.BLOCKED;
        this.failureMessage = requireText(
                reason,
                "block reason"
        );
    }

    public synchronized void cancel() {
        if (status == TaskStatus.PENDING ||
                status == TaskStatus.BLOCKED) {
            status = TaskStatus.CANCELLED;
        }
    }

    public boolean canRetry() {
        return attempt < maxAttempts;
    }

    public boolean isTerminal() {
        return status == TaskStatus.SUCCEEDED ||
                status == TaskStatus.FAILED ||
                status == TaskStatus.CANCELLED;
    }

    private static String requireText(
            String value,
            String field
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }

        return value;
    }
}