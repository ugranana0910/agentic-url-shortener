package com.navya.agentic_url_shortener.artifact;

import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
public class ArtifactReference {

    UUID id;
    UUID workflowId;
    int workflowRevision;
    UUID taskId;
    ArtifactType type;
    String name;
    String relativePath;
    String sha256;
    long sizeBytes;
    Instant createdAt;
}