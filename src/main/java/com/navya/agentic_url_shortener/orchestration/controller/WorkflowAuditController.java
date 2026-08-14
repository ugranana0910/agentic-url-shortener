package com.navya.agentic_url_shortener.orchestration.controller;

import com.navya.agentic_url_shortener.audit.AuditEvent;
import com.navya.agentic_url_shortener.audit.AuditJournal;
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
        "/api/v1/engineering-workflows/{workflowId}/audit-events"
)
public class WorkflowAuditController {

    private final WorkflowRepository workflowRepository;
    private final AuditJournal auditJournal;

    @GetMapping
    public List<AuditEvent> list(
            @PathVariable UUID workflowId
    ) {
        workflowRepository.findById(workflowId)
                .orElseThrow(
                        () -> new WorkflowNotFoundException(
                                workflowId
                        )
                );

        return auditJournal.findByWorkflowId(workflowId);
    }
}