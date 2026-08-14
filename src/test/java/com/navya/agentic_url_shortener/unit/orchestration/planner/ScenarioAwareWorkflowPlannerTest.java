package com.navya.agentic_url_shortener.unit.orchestration.planner;

import com.navya.agentic_url_shortener.agent.requirement.DeterministicRequirementAnalyzer;
import com.navya.agentic_url_shortener.orchestration.domain.ScenarioType;
import com.navya.agentic_url_shortener.orchestration.domain.TaskType;
import com.navya.agentic_url_shortener.orchestration.planner.ScenarioAwareWorkflowPlanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScenarioAwareWorkflowPlannerTest {

    private ScenarioAwareWorkflowPlanner planner;

    @BeforeEach
    void setUp() {
        planner = new ScenarioAwareWorkflowPlanner(
                new DeterministicRequirementAnalyzer(),
                Clock.fixed(
                        Instant.parse(
                                "2026-08-13T12:00:00Z"
                        ),
                        ZoneOffset.UTC
                )
        );
    }

    @Test
    void brownfieldPlanIncludesRepositoryAnalysis() {
        var planned = planner.plan(
                ScenarioType.BROWNFIELD,
                "Add redirect analytics to the existing URL API",
                "./repository"
        );

        assertThat(planned.getWorkflow().getTasks())
                .extracting(task -> task.getType())
                .contains(
                        TaskType.REQUIREMENT_ANALYSIS,
                        TaskType.REPOSITORY_ANALYSIS,
                        TaskType.ARCHITECTURE_DESIGN,
                        TaskType.IMPLEMENTATION,
                        TaskType.TEST_PLANNING,
                        TaskType.VALIDATION,
                        TaskType.DOCUMENTATION,
                        TaskType.RELEASE_READINESS
                );
    }

    @Test
    void greenfieldPlanDoesNotRequireRepositoryAnalysis() {
        var planned = planner.plan(
                ScenarioType.GREENFIELD,
                "Create a URL shortening API",
                null
        );

        assertThat(planned.getWorkflow().getTasks())
                .extracting(task -> task.getType())
                .doesNotContain(
                        TaskType.REPOSITORY_ANALYSIS
                );
    }

    @Test
    void documentationPlanAvoidsImplementation() {
        var planned = planner.plan(
                ScenarioType.BROWNFIELD,
                "Update the README setup instructions",
                "./repository"
        );

        assertThat(planned.getWorkflow().getTasks())
                .extracting(task -> task.getType())
                .contains(
                        TaskType.REQUIREMENT_ANALYSIS,
                        TaskType.DOCUMENTATION,
                        TaskType.VALIDATION
                )
                .doesNotContain(
                        TaskType.IMPLEMENTATION,
                        TaskType.ARCHITECTURE_DESIGN
                );
    }

    @Test
    void ambiguousPlanStopsAfterRequirementAnalysis() {
        var planned = planner.plan(
                ScenarioType.AMBIGUOUS,
                "Make shortened links safer and better",
                null
        );

        assertThat(planned.getAnalysis()
                .requiresClarification()).isTrue();

        assertThat(planned.getWorkflow().getTasks())
                .hasSize(1)
                .extracting(task -> task.getType())
                .containsExactly(
                        TaskType.REQUIREMENT_ANALYSIS
                );
    }

    @Test
    void highRiskPlanIncludesSecurityReview() {
        var planned = planner.plan(
                ScenarioType.GREENFIELD,
                "Add authentication to the public URL API",
                null
        );

        assertThat(planned.getWorkflow().getTasks())
                .extracting(task -> task.getType())
                .contains(TaskType.SECURITY_REVIEW);
    }

    @Test
    void brownfieldScenarioRequiresRepositoryPath() {
        assertThatThrownBy(
                () -> planner.plan(
                        ScenarioType.BROWNFIELD,
                        "Add redirect analytics",
                        null
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "repositoryPath is required for a brownfield scenario"
                );
    }
}