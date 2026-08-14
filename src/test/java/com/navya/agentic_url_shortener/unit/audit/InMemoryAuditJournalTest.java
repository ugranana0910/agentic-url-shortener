package com.navya.agentic_url_shortener.unit.audit;

import com.navya.agentic_url_shortener.audit.AuditEventType;
import com.navya.agentic_url_shortener.audit.InMemoryAuditJournal;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryAuditJournalTest {

    @Test
    void recordsWorkflowTimeline() {
        Instant now =
                Instant.parse("2026-08-13T12:00:00Z");

        InMemoryAuditJournal journal =
                new InMemoryAuditJournal(
                        Clock.fixed(
                                now,
                                ZoneOffset.UTC
                        )
                );

        UUID workflowId = UUID.randomUUID();

        journal.record(
                workflowId,
                1,
                null,
                AuditEventType.WORKFLOW_CREATED,
                "SYSTEM",
                "Created",
                Map.of()
        );

        journal.record(
                workflowId,
                1,
                UUID.randomUUID(),
                AuditEventType.TASK_STARTED,
                "SYSTEM",
                "Started",
                Map.of("attempt", 1)
        );

        assertThat(
                journal.findByWorkflowId(workflowId)
        )
                .hasSize(2)
                .extracting(event -> event.getType())
                .containsExactly(
                        AuditEventType.WORKFLOW_CREATED,
                        AuditEventType.TASK_STARTED
                );
    }
}