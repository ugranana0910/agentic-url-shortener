package com.navya.agentic_url_shortener.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@RequiredArgsConstructor
public class InMemoryAuditJournal
        implements AuditJournal {

    private final Clock clock;

    private final ConcurrentMap<UUID, List<AuditEvent>>
            events = new ConcurrentHashMap<>();

    @Override
    public AuditEvent record(
            UUID workflowId,
            int workflowRevision,
            UUID taskId,
            AuditEventType type,
            String actor,
            String detail,
            Map<String, Object> attributes
    ) {
        AuditEvent event = new AuditEvent(
                UUID.randomUUID(),
                workflowId,
                workflowRevision,
                taskId,
                type,
                actor == null || actor.isBlank()
                        ? "SYSTEM"
                        : actor.trim(),
                detail,
                attributes == null
                        ? Map.of()
                        : Map.copyOf(attributes),
                clock.instant()
        );

        events.compute(
                workflowId,
                (ignored, existing) -> {
                    List<AuditEvent> updated =
                            existing == null
                                    ? new ArrayList<>()
                                    : new ArrayList<>(existing);

                    updated.add(event);

                    return List.copyOf(updated);
                }
        );

        return event;
    }

    @Override
    public List<AuditEvent> findByWorkflowId(
            UUID workflowId
    ) {
        return events.getOrDefault(
                        workflowId,
                        List.of()
                )
                .stream()
                .sorted(
                        Comparator.comparing(
                                AuditEvent::getOccurredAt
                        )
                )
                .toList();
    }
}