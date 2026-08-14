package com.navya.agentic_url_shortener.governance;

import com.navya.agentic_url_shortener.artifact.ArtifactReference;
import com.navya.agentic_url_shortener.artifact.ArtifactStore;
import com.navya.agentic_url_shortener.orchestration.domain.EngineeringWorkflow;
import com.navya.agentic_url_shortener.audit.AuditEventType;
import com.navya.agentic_url_shortener.audit.AuditJournal;
import com.navya.agentic_url_shortener.audit.WorkflowMetrics;
import com.navya.agentic_url_shortener.orchestration.dto.WorkflowResponse;
import com.navya.agentic_url_shortener.orchestration.engine.WorkflowEngine;
import com.navya.agentic_url_shortener.orchestration.exception.WorkflowNotFoundException;
import com.navya.agentic_url_shortener.orchestration.repository.WorkflowRepository;
import com.navya.agentic_url_shortener.policy.PolicyCheckResult;
import com.navya.agentic_url_shortener.policy.ReleasePolicyEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowGovernanceService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowEngine workflowEngine;
    private final ArtifactStore artifactStore;
    private final ReleasePolicyEngine policyEngine;
    private final Clock clock;
    private final AuditJournal auditJournal;
    private final WorkflowMetrics workflowMetrics;

    public WorkflowResponse approve(
            UUID workflowId,
            String actor,
            String reason
    ) {
        String approver = requireActor(actor);

        EngineeringWorkflow workflow =
                requireWorkflow(workflowId);

        synchronized (workflow) {
            if (!workflow.isWaitingForApproval()) {
                throw new GovernanceException(
                        "Workflow is not awaiting approval"
                );
            }

            List<ArtifactReference> artifacts =
                    artifactStore.findByWorkflowId(
                            workflowId
                    );

            policyEngine.enforce(
                    workflow,
                    artifacts
            );

            List<String> hashes = artifacts.stream()
                    .filter(artifact ->
                            artifact.getWorkflowRevision()
                                    == workflow.getRevision()
                    )
                    .map(ArtifactReference::getSha256)
                    .sorted()
                    .toList();

            ApprovalRecord approval =
                    new ApprovalRecord(
                            workflow.getId(),
                            workflow.getRevision(),
                            approver,
                            reason,
                            hashes,
                            clock.instant()
                    );

            workflow.getContext().put(
                    approvalKey(workflow),
                    approval
            );

            workflow.getContext().put(
                    "releasePolicyResults",
                    policyEngine.evaluate(
                            workflow,
                            artifacts
                    )
            );

            auditJournal.record(
                    workflow.getId(),
                    workflow.getRevision(),
                    null,
                    AuditEventType.POLICY_EVALUATED,
                    approver,
                    "Release policies evaluated successfully",
                    Map.of(
                            "artifactCount",
                            artifacts.size()
                    )
            );

            auditJournal.record(
                    workflow.getId(),
                    workflow.getRevision(),
                    null,
                    AuditEventType.APPROVAL_GRANTED,
                    approver,
                    reason,
                    Map.of(
                            "artifactHashes",
                            hashes
                    )
            );

            workflowMetrics.approvalGranted();

            workflow.resume();
        }

        EngineeringWorkflow completed =
                workflowEngine.resume(workflow);

        return WorkflowResponse.from(completed);
    }

    public WorkflowResponse safeStop(
            UUID workflowId,
            String actor,
            String reason
    ) {
        String stoppedBy = requireActor(actor);

        EngineeringWorkflow workflow =
                requireWorkflow(workflowId);

        synchronized (workflow) {
            workflow.safeStop(
                    clock.instant(),
                    reason,
                    stoppedBy
            );
            auditJournal.record(
                    workflow.getId(),
                    workflow.getRevision(),
                    null,
                    AuditEventType.SAFE_STOPPED,
                    stoppedBy,
                    reason,
                    Map.of()
            );

            workflowMetrics.safeStopped();

            workflowRepository.save(workflow);
        }

        return WorkflowResponse.from(workflow);
    }

    public List<PolicyCheckResult> policies(
            UUID workflowId
    ) {
        EngineeringWorkflow workflow =
                requireWorkflow(workflowId);

        return policyEngine.evaluate(
                workflow,
                artifactStore.findByWorkflowId(workflowId)
        );
    }

    private EngineeringWorkflow requireWorkflow(
            UUID workflowId
    ) {
        return workflowRepository.findById(workflowId)
                .orElseThrow(
                        () -> new WorkflowNotFoundException(
                                workflowId
                        )
                );
    }

    private String requireActor(String actor) {
        if (actor == null || actor.isBlank()) {
            throw new GovernanceException(
                    "X-Actor header is required"
            );
        }

        String normalized = actor.trim();

        if (normalized.length() > 100) {
            throw new GovernanceException(
                    "X-Actor header must not exceed 100 characters"
            );
        }

        return normalized;
    }

    private String approvalKey(
            EngineeringWorkflow workflow
    ) {
        return "approval:"
                + releaseReadinessTaskId(workflow)
                + ":revision:"
                + workflow.getRevision();
    }

    private UUID releaseReadinessTaskId(
            EngineeringWorkflow workflow
    ) {
        return workflow.getTasks()
                .stream()
                .filter(task ->
                        task.getType()
                                == com.navya.agentic_url_shortener
                                .orchestration.domain.TaskType
                                .RELEASE_READINESS
                )
                .map(task -> task.getId())
                .findFirst()
                .orElseThrow(
                        () -> new GovernanceException(
                                "Workflow has no release-readiness task"
                        )
                );
    }
}