package com.navya.agentic_url_shortener.policy;

import com.navya.agentic_url_shortener.artifact.ArtifactReference;
import com.navya.agentic_url_shortener.artifact.ArtifactType;
import com.navya.agentic_url_shortener.governance.PolicyViolationException;
import com.navya.agentic_url_shortener.orchestration.domain.EngineeringWorkflow;
import com.navya.agentic_url_shortener.orchestration.domain.TaskStatus;
import com.navya.agentic_url_shortener.orchestration.domain.TaskType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ReleasePolicyEngine {

    public List<PolicyCheckResult> evaluate(
            EngineeringWorkflow workflow,
            List<ArtifactReference> artifacts
    ) {
        List<PolicyCheckResult> results =
                new ArrayList<>();

        boolean validationSucceeded =
                workflow.getTasks()
                        .stream()
                        .filter(task ->
                                task.getType()
                                        == TaskType.VALIDATION
                        )
                        .allMatch(task ->
                                task.getStatus()
                                        == TaskStatus.SUCCEEDED
                        );

        results.add(
                new PolicyCheckResult(
                        "POL-VALIDATION-PASSED",
                        validationSucceeded,
                        validationSucceeded
                                ? "All validation tasks succeeded"
                                : "One or more validation tasks did not succeed"
                )
        );

        boolean validationArtifactExists =
                artifacts.stream()
                        .anyMatch(artifact ->
                                artifact.getType()
                                        == ArtifactType.VALIDATION_REPORT &&
                                        artifact.getWorkflowRevision()
                                                == workflow.getRevision()
                        );

        results.add(
                new PolicyCheckResult(
                        "POL-VALIDATION-EVIDENCE",
                        validationArtifactExists,
                        validationArtifactExists
                                ? "Current-revision validation evidence exists"
                                : "Current-revision validation evidence is missing"
                )
        );

        boolean architectureArtifactExists =
                artifacts.stream()
                        .anyMatch(artifact ->
                                artifact.getType()
                                        == ArtifactType.ARCHITECTURE &&
                                        artifact.getWorkflowRevision()
                                                == workflow.getRevision()
                        );

        results.add(
                new PolicyCheckResult(
                        "POL-ARCHITECTURE-EVIDENCE",
                        architectureArtifactExists,
                        architectureArtifactExists
                                ? "Current-revision architecture evidence exists"
                                : "Current-revision architecture evidence is missing"
                )
        );

        return List.copyOf(results);
    }

    public void enforce(
            EngineeringWorkflow workflow,
            List<ArtifactReference> artifacts
    ) {
        List<PolicyCheckResult> failed =
                evaluate(workflow, artifacts)
                        .stream()
                        .filter(result -> !result.isPassed())
                        .toList();

        if (!failed.isEmpty()) {
            String failedPolicies = failed.stream()
                    .map(PolicyCheckResult::getPolicy)
                    .sorted()
                    .collect(
                            java.util.stream.Collectors
                                    .joining(", ")
                    );

            throw new PolicyViolationException(
                    "Release approval blocked by policies: "
                            + failedPolicies
            );
        }
    }
}