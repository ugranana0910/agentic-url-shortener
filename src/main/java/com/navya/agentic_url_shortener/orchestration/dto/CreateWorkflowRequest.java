package com.navya.agentic_url_shortener.orchestration.dto;

import com.navya.agentic_url_shortener.orchestration.domain.ScenarioType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateWorkflowRequest {

    @NotNull(message = "scenarioType is required")
    private ScenarioType scenarioType;

    @NotBlank(message = "requirement is required")
    @Size(
            max = 10_000,
            message = "requirement must not exceed 10000 characters"
    )
    private String requirement;

    @Size(
            max = 1_024,
            message = "repositoryPath must not exceed 1024 characters"
    )
    private String repositoryPath;
}