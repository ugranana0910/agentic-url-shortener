package com.navya.agentic_url_shortener.governance;

import lombok.Value;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Value
public class ApprovalRecord {

    UUID workflowId;
    int workflowRevision;
    String approver;
    String reason;
    List<String> artifactHashes;
    Instant approvedAt;
}