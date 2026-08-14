package com.navya.agentic_url_shortener.audit;

import lombok.Value;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Value
public class AuditEvent {

    UUID id;
    UUID workflowId;
    int workflowRevision;
    UUID taskId;
    AuditEventType type;
    String actor;
    String detail;
    Map<String, Object> attributes;
    Instant occurredAt;
}