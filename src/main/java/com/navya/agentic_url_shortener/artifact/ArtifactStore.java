package com.navya.agentic_url_shortener.artifact;

import java.util.List;
import java.util.UUID;

public interface ArtifactStore {

    ArtifactReference write(
            UUID workflowId,
            int workflowRevision,
            UUID taskId,
            ArtifactType type,
            String name,
            String content
    );

    List<ArtifactReference> findByWorkflowId(
            UUID workflowId
    );
}