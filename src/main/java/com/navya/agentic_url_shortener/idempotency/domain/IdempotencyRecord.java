package com.navya.agentic_url_shortener.idempotency.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
@Entity
@Table(name = "idempotency_records")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdempotencyRecord {

    @Id
    @Column(
            name = "idempotency_key",
            nullable = false,
            length = 128,
            updatable = false
    )
    private String key;

    @Column(
            name = "request_hash",
            nullable = false,
            length = 64,
            updatable = false
    )
    private String requestHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IdempotencyStatus status;

    @Column(name = "resource_id")
    private UUID resourceId;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Version
    @Column(nullable = false)
    private long version;

    public IdempotencyRecord(
            String key,
            String requestHash,
            Instant createdAt,
            Instant expiresAt
    ) {
        this.key = Objects.requireNonNull(key);
        this.requestHash = Objects.requireNonNull(requestHash);
        this.status = IdempotencyStatus.IN_PROGRESS;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = createdAt;
        this.expiresAt = Objects.requireNonNull(expiresAt);
    }

    public boolean hasRequestHash(String candidateHash) {
        return requestHash.equals(candidateHash);
    }

    public boolean isCompleted() {
        return status == IdempotencyStatus.COMPLETED;
    }

    public boolean isInProgress() {
        return status == IdempotencyStatus.IN_PROGRESS;
    }

    public boolean isFailed() {
        return status == IdempotencyStatus.FAILED;
    }

    public boolean isExpiredAt(Instant instant) {
        return !expiresAt.isAfter(instant);
    }

    public boolean hasTimedOutAt(
            Instant instant,
            Duration timeout
    ) {
        return isInProgress() &&
                !updatedAt.plus(timeout).isAfter(instant);
    }

    public void complete(
            UUID resourceId,
            int responseStatus,
            Instant completedAt
    ) {
        if (!isInProgress()) {
            throw new IllegalStateException(
                    "Only an in-progress record can be completed"
            );
        }

        this.status = IdempotencyStatus.COMPLETED;
        this.resourceId = Objects.requireNonNull(resourceId);
        this.responseStatus = responseStatus;
        this.updatedAt = Objects.requireNonNull(completedAt);
    }

    public void fail(Instant failedAt) {
        if (isCompleted()) {
            return;
        }

        this.status = IdempotencyStatus.FAILED;
        this.resourceId = null;
        this.responseStatus = null;
        this.updatedAt = Objects.requireNonNull(failedAt);
    }

    public void restart(
            Instant restartedAt,
            Instant newExpiresAt
    ) {
        if (isCompleted()) {
            throw new IllegalStateException(
                    "A completed record cannot be restarted"
            );
        }

        this.status = IdempotencyStatus.IN_PROGRESS;
        this.resourceId = null;
        this.responseStatus = null;
        this.updatedAt = Objects.requireNonNull(restartedAt);
        this.expiresAt = Objects.requireNonNull(newExpiresAt);
    }
}