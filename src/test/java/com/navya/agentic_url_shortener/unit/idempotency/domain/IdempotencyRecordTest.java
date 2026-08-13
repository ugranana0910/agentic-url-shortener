package com.navya.agentic_url_shortener.unit.idempotency.domain;

import com.navya.agentic_url_shortener.idempotency.domain.IdempotencyRecord;
import com.navya.agentic_url_shortener.idempotency.domain.IdempotencyStatus;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdempotencyRecordTest {

    private static final Instant NOW =
            Instant.parse("2026-08-13T12:00:00Z");

    @Test
    void startsInProgress() {
        IdempotencyRecord record = createRecord();

        assertThat(record.getStatus())
                .isEqualTo(IdempotencyStatus.IN_PROGRESS);

        assertThat(record.isInProgress()).isTrue();
    }

    @Test
    void completesWithResourceAndStatus() {
        IdempotencyRecord record = createRecord();
        UUID resourceId = UUID.randomUUID();

        record.complete(
                resourceId,
                201,
                NOW.plusSeconds(1)
        );

        assertThat(record.isCompleted()).isTrue();
        assertThat(record.getResourceId())
                .isEqualTo(resourceId);
        assertThat(record.getResponseStatus())
                .isEqualTo(201);
    }

    @Test
    void detectsTimedOutExecution() {
        IdempotencyRecord record = createRecord();

        assertThat(
                record.hasTimedOutAt(
                        NOW.plus(Duration.ofMinutes(2)),
                        Duration.ofMinutes(2)
                )
        ).isTrue();
    }

    @Test
    void completedRecordCannotRestart() {
        IdempotencyRecord record = createRecord();

        record.complete(
                UUID.randomUUID(),
                201,
                NOW.plusSeconds(1)
        );

        assertThatThrownBy(
                () -> record.restart(
                        NOW.plusSeconds(2),
                        NOW.plusSeconds(3600)
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "A completed record cannot be restarted"
                );
    }

    private IdempotencyRecord createRecord() {
        return new IdempotencyRecord(
                "request-123",
                "a".repeat(64),
                NOW,
                NOW.plusSeconds(3600)
        );
    }
}