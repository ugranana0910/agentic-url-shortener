package com.navya.agentic_url_shortener.orchestration.planner;

import com.navya.agentic_url_shortener.agent.requirement.RequirementAnalysis;
import com.navya.agentic_url_shortener.agent.requirement.RequirementAnalyzer;
import com.navya.agentic_url_shortener.agent.requirement.RiskLevel;
import com.navya.agentic_url_shortener.orchestration.domain.EngineeringWorkflow;
import com.navya.agentic_url_shortener.orchestration.domain.GateDefinition;
import com.navya.agentic_url_shortener.orchestration.domain.ScenarioType;
import com.navya.agentic_url_shortener.orchestration.domain.TaskType;
import com.navya.agentic_url_shortener.orchestration.domain.WorkflowTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ScenarioAwareWorkflowPlanner
        implements WorkflowPlanner {

    private static final int DEFAULT_MAX_ATTEMPTS = 2;

    private final RequirementAnalyzer requirementAnalyzer;
    private final Clock clock;

    @Override
    public PlannedWorkflow plan(
            ScenarioType scenarioType,
            String requirement,
            String repositoryPath
    ) {
        validateRepositoryPath(
                scenarioType,
                repositoryPath
        );

        RequirementAnalysis analysis =
                requirementAnalyzer.analyze(
                        requirement,
                        scenarioType
                );

        EngineeringWorkflow workflow =
                new EngineeringWorkflow(
                        UUID.randomUUID(),
                        scenarioType,
                        analysis.getNormalizedRequirement(),
                        normalizeRepositoryPath(repositoryPath),
                        clock.instant()
                );

        addAnalysisToContext(workflow, analysis);

        if (analysis.requiresClarification()) {
            addClarificationPlan(workflow);
            return new PlannedWorkflow(
                    workflow,
                    analysis
            );
        }

        if (analysis.isDocumentationOnly()) {
            addDocumentationPlan(workflow);
            return new PlannedWorkflow(
                    workflow,
                    analysis
            );
        }

        if (scenarioType == ScenarioType.GREENFIELD) {
            addGreenfieldPlan(workflow, analysis);
        } else {
            addBrownfieldPlan(workflow, analysis);
        }

        return new PlannedWorkflow(
                workflow,
                analysis
        );
    }

    private void addClarificationPlan(
            EngineeringWorkflow workflow
    ) {
        WorkflowTask analysis = task(
                "Analyze requirement and identify ambiguities",
                TaskType.REQUIREMENT_ANALYSIS,
                Set.of(),
                GateDefinition.dependenciesSucceeded(),
                GateDefinition.contextKeyPresent(
                        "requirementAnalysis"
                )
        );

        workflow.addTask(analysis);
    }

    private void addDocumentationPlan(
            EngineeringWorkflow workflow
    ) {
        WorkflowTask analysis = task(
                "Analyze documentation requirement",
                TaskType.REQUIREMENT_ANALYSIS,
                Set.of(),
                GateDefinition.dependenciesSucceeded(),
                GateDefinition.contextKeyPresent(
                        "requirementAnalysis"
                )
        );

        WorkflowTask documentation = task(
                "Generate documentation update",
                TaskType.DOCUMENTATION,
                Set.of(analysis.getId()),
                GateDefinition.dependenciesSucceeded(),
                GateDefinition.contextKeyPresent(
                        "documentationOutput"
                )
        );

        WorkflowTask validation = task(
                "Validate documentation",
                TaskType.VALIDATION,
                Set.of(documentation.getId()),
                GateDefinition.dependenciesSucceeded(),
                GateDefinition.contextKeyPresent(
                        "validationOutput"
                )
        );

        workflow.addTask(analysis);
        workflow.addTask(documentation);
        workflow.addTask(validation);
    }

    private void addGreenfieldPlan(
            EngineeringWorkflow workflow,
            RequirementAnalysis analysis
    ) {
        WorkflowTask requirement = task(
                "Normalize greenfield requirement",
                TaskType.REQUIREMENT_ANALYSIS,
                Set.of(),
                GateDefinition.dependenciesSucceeded(),
                GateDefinition.contextKeyPresent(
                        "requirementAnalysis"
                )
        );

        WorkflowTask architecture = task(
                "Design greenfield architecture",
                TaskType.ARCHITECTURE_DESIGN,
                Set.of(requirement.getId()),
                GateDefinition.dependenciesSucceeded(),
                GateDefinition.contextKeyPresent(
                        "architectureOutput"
                )
        );

        WorkflowTask implementation = task(
                "Generate implementation",
                TaskType.IMPLEMENTATION,
                Set.of(architecture.getId()),
                GateDefinition.dependenciesSucceeded(),
                GateDefinition.contextKeyPresent(
                        "implementationOutput"
                )
        );

        WorkflowTask testPlanning = task(
                "Generate test strategy",
                TaskType.TEST_PLANNING,
                Set.of(architecture.getId()),
                GateDefinition.dependenciesSucceeded(),
                GateDefinition.contextKeyPresent(
                        "testPlanOutput"
                )
        );

        WorkflowTask validation = task(
                "Validate implementation and tests",
                TaskType.VALIDATION,
                Set.of(
                        implementation.getId(),
                        testPlanning.getId()
                ),
                GateDefinition.dependenciesSucceeded(),
                GateDefinition.contextKeyPresent(
                        "validationOutput"
                )
        );

        workflow.addTask(requirement);
        workflow.addTask(architecture);
        workflow.addTask(implementation);
        workflow.addTask(testPlanning);
        workflow.addTask(validation);

        addReleaseTasks(
                workflow,
                analysis,
                validation
        );
    }

    private void addBrownfieldPlan(
            EngineeringWorkflow workflow,
            RequirementAnalysis analysis
    ) {
        WorkflowTask requirement = task(
                "Normalize brownfield requirement",
                TaskType.REQUIREMENT_ANALYSIS,
                Set.of(),
                GateDefinition.dependenciesSucceeded(),
                GateDefinition.contextKeyPresent(
                        "requirementAnalysis"
                )
        );

        WorkflowTask repositoryAnalysis = task(
                "Analyze impacted repository components",
                TaskType.REPOSITORY_ANALYSIS,
                Set.of(requirement.getId()),
                GateDefinition.dependenciesSucceeded(),
                GateDefinition.contextKeyPresent(
                        "repositoryAnalysisOutput"
                )
        );

        WorkflowTask architecture = task(
                "Design compatible change",
                TaskType.ARCHITECTURE_DESIGN,
                Set.of(repositoryAnalysis.getId()),
                GateDefinition.dependenciesSucceeded(),
                GateDefinition.contextKeyPresent(
                        "architectureOutput"
                )
        );

        WorkflowTask implementation = task(
                "Generate brownfield implementation",
                TaskType.IMPLEMENTATION,
                Set.of(architecture.getId()),
                GateDefinition.dependenciesSucceeded(),
                GateDefinition.contextKeyPresent(
                        "implementationOutput"
                )
        );

        WorkflowTask testPlanning = task(
                "Plan regression and feature tests",
                TaskType.TEST_PLANNING,
                Set.of(architecture.getId()),
                GateDefinition.dependenciesSucceeded(),
                GateDefinition.contextKeyPresent(
                        "testPlanOutput"
                )
        );

        WorkflowTask validation = task(
                "Validate change and regression safety",
                TaskType.VALIDATION,
                Set.of(
                        implementation.getId(),
                        testPlanning.getId()
                ),
                GateDefinition.dependenciesSucceeded(),
                GateDefinition.contextKeyPresent(
                        "validationOutput"
                )
        );

        workflow.addTask(requirement);
        workflow.addTask(repositoryAnalysis);
        workflow.addTask(architecture);
        workflow.addTask(implementation);
        workflow.addTask(testPlanning);
        workflow.addTask(validation);

        addReleaseTasks(
                workflow,
                analysis,
                validation
        );
    }

    private void addReleaseTasks(
            EngineeringWorkflow workflow,
            RequirementAnalysis analysis,
            WorkflowTask validation
    ) {
        Set<UUID> releaseDependencies =
                new LinkedHashSet<>();

        releaseDependencies.add(validation.getId());

        if (analysis.getRiskLevel()
                == RiskLevel.HIGH) {
            WorkflowTask securityReview = task(
                    "Perform security and change review",
                    TaskType.SECURITY_REVIEW,
                    Set.of(validation.getId()),
                    GateDefinition.dependenciesSucceeded(),
                    GateDefinition.contextKeyPresent(
                            "securityReviewOutput"
                    )
            );

            workflow.addTask(securityReview);
            releaseDependencies.add(
                    securityReview.getId()
            );
        }

        WorkflowTask documentation = task(
                "Generate supporting documentation",
                TaskType.DOCUMENTATION,
                Set.of(validation.getId()),
                GateDefinition.dependenciesSucceeded(),
                GateDefinition.contextKeyPresent(
                        "documentationOutput"
                )
        );

        workflow.addTask(documentation);
        releaseDependencies.add(documentation.getId());

        WorkflowTask releaseReadiness = task(
                "Assess release readiness",
                TaskType.RELEASE_READINESS,
                releaseDependencies,
                GateDefinition.dependenciesSucceeded(),
                GateDefinition.contextKeyPresent(
                        "releaseReadinessOutput"
                )
        );

        workflow.addTask(releaseReadiness);
    }

    private WorkflowTask task(
            String name,
            TaskType type,
            Set<UUID> dependencies,
            GateDefinition entryGate,
            GateDefinition exitGate
    ) {
        return new WorkflowTask(
                UUID.randomUUID(),
                name,
                type,
                dependencies,
                entryGate,
                exitGate,
                DEFAULT_MAX_ATTEMPTS
        );
    }

    private void addAnalysisToContext(
            EngineeringWorkflow workflow,
            RequirementAnalysis analysis
    ) {
        workflow.getContext().put(
                "normalizedRequirement",
                analysis.getNormalizedRequirement()
        );

        workflow.getContext().put(
                "acceptanceCriteria",
                analysis.getAcceptanceCriteria()
        );

        workflow.getContext().put(
                "ambiguities",
                analysis.getAmbiguities()
        );

        workflow.getContext().put(
                "assumptions",
                analysis.getAssumptions()
        );

        workflow.getContext().put(
                "riskLevel",
                analysis.getRiskLevel().name()
        );

        workflow.getContext().put(
                "documentationOnly",
                analysis.isDocumentationOnly()
        );
    }

    private void validateRepositoryPath(
            ScenarioType scenarioType,
            String repositoryPath
    ) {
        if (scenarioType == ScenarioType.BROWNFIELD &&
                (repositoryPath == null ||
                        repositoryPath.isBlank())) {
            throw new IllegalArgumentException(
                    "repositoryPath is required for a brownfield scenario"
            );
        }
    }

    private String normalizeRepositoryPath(
            String repositoryPath
    ) {
        if (repositoryPath == null ||
                repositoryPath.isBlank()) {
            return null;
        }

        return repositoryPath.trim();
    }
}