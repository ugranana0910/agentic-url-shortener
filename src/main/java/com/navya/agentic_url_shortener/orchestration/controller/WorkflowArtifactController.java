package com.navya.agentic_url_shortener.orchestration.controller;

import com.navya.agentic_url_shortener.artifact.ArtifactReference;
import com.navya.agentic_url_shortener.artifact.ArtifactStore;
import com.navya.agentic_url_shortener.orchestration.exception.WorkflowNotFoundException;
import com.navya.agentic_url_shortener.orchestration.repository.WorkflowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(
        "/api/v1/engineering-workflows/{workflowId}/artifacts"
)
public class WorkflowArtifactController {

    private final WorkflowRepository workflowRepository;
    private final ArtifactStore artifactStore;

    @GetMapping
    public List<ArtifactReference> list(
            @PathVariable UUID workflowId
    ) {
        workflowRepository.findById(workflowId)
                .orElseThrow(
                        () -> new WorkflowNotFoundException(
                                workflowId
                        )
                );

        return artifactStore.findByWorkflowId(workflowId);
    }
}