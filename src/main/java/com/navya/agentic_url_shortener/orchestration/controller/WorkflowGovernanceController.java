package com.navya.agentic_url_shortener.orchestration.controller;

import com.navya.agentic_url_shortener.governance.ApproveWorkflowRequest;
import com.navya.agentic_url_shortener.governance.SafeStopWorkflowRequest;
import com.navya.agentic_url_shortener.governance.WorkflowGovernanceService;
import com.navya.agentic_url_shortener.orchestration.dto.WorkflowResponse;
import com.navya.agentic_url_shortener.policy.PolicyCheckResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(
        "/api/v1/engineering-workflows/{workflowId}/governance"
)
public class WorkflowGovernanceController {

    private static final String ACTOR_HEADER = "X-Actor";

    private final WorkflowGovernanceService governanceService;

    @GetMapping("/policies")
    public List<PolicyCheckResult> policies(
            @PathVariable UUID workflowId
    ) {
        return governanceService.policies(workflowId);
    }

    @PostMapping("/approvals/release-readiness")
    public WorkflowResponse approveRelease(
            @PathVariable UUID workflowId,

            @RequestHeader(
                    name = ACTOR_HEADER,
                    required = false
            )
            String actor,

            @Valid @RequestBody
            ApproveWorkflowRequest request
    ) {
        return governanceService.approve(
                workflowId,
                actor,
                request.getReason()
        );
    }

    @PostMapping("/safe-stop")
    public WorkflowResponse safeStop(
            @PathVariable UUID workflowId,

            @RequestHeader(
                    name = ACTOR_HEADER,
                    required = false
            )
            String actor,

            @Valid @RequestBody
            SafeStopWorkflowRequest request
    ) {
        return governanceService.safeStop(
                workflowId,
                actor,
                request.getReason()
        );
    }
}