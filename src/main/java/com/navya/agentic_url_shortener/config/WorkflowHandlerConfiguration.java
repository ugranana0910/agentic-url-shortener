package com.navya.agentic_url_shortener.config;

import com.navya.agentic_url_shortener.orchestration.domain.TaskType;
import com.navya.agentic_url_shortener.orchestration.engine.DeterministicStageHandler;
import com.navya.agentic_url_shortener.orchestration.engine.WorkflowTaskHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkflowHandlerConfiguration {

    @Bean
    public WorkflowTaskHandler repositoryAnalysisHandler() {
        return new DeterministicStageHandler(
                TaskType.REPOSITORY_ANALYSIS,
                "repositoryAnalysisOutput"
        );
    }

    @Bean
    public WorkflowTaskHandler architectureHandler() {
        return new DeterministicStageHandler(
                TaskType.ARCHITECTURE_DESIGN,
                "architectureOutput"
        );
    }

    @Bean
    public WorkflowTaskHandler implementationHandler() {
        return new DeterministicStageHandler(
                TaskType.IMPLEMENTATION,
                "implementationOutput"
        );
    }

    @Bean
    public WorkflowTaskHandler testPlanningHandler() {
        return new DeterministicStageHandler(
                TaskType.TEST_PLANNING,
                "testPlanOutput"
        );
    }

    @Bean
    public WorkflowTaskHandler validationHandler() {
        return new DeterministicStageHandler(
                TaskType.VALIDATION,
                "validationOutput"
        );
    }

    @Bean
    public WorkflowTaskHandler securityReviewHandler() {
        return new DeterministicStageHandler(
                TaskType.SECURITY_REVIEW,
                "securityReviewOutput"
        );
    }

    @Bean
    public WorkflowTaskHandler documentationHandler() {
        return new DeterministicStageHandler(
                TaskType.DOCUMENTATION,
                "documentationOutput"
        );
    }

    @Bean
    public WorkflowTaskHandler releaseReadinessHandler() {
        return new DeterministicStageHandler(
                TaskType.RELEASE_READINESS,
                "releaseReadinessOutput"
        );
    }
}