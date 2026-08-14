package com.navya.agentic_url_shortener.governance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SafeStopWorkflowRequest {

    @NotBlank(message = "reason is required")
    @Size(
            max = 1000,
            message = "reason must not exceed 1000 characters"
    )
    private String reason;
}