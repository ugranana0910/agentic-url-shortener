package com.navya.agentic_url_shortener.orchestration.controller;

import com.navya.agentic_url_shortener.orchestration.dto.CreateWorkflowRequest;
import com.navya.agentic_url_shortener.orchestration.dto.WorkflowResponse;
import com.navya.agentic_url_shortener.orchestration.service.EngineeringWorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/engineering-workflows")
public class EngineeringWorkflowController {

    private final EngineeringWorkflowService workflowService;

    @PostMapping
    public ResponseEntity<WorkflowResponse> create(
            @Valid @RequestBody
            CreateWorkflowRequest request
    ) {
        WorkflowResponse response =
                workflowService.createAndExecute(request);

        return ResponseEntity
                .created(
                        URI.create(
                                "/api/v1/engineering-workflows/"
                                        + response.getId()
                        )
                )
                .body(response);
    }

    @GetMapping("/{workflowId}")
    public WorkflowResponse get(
            @PathVariable UUID workflowId
    ) {
        return workflowService.get(workflowId);
    }
}