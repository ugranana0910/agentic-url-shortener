package com.navya.agentic_url_shortener.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "agentic")
public record AgenticProperties(
        Workspace workspace,
        Execution execution,
        Model model
) {

    public record Workspace(Path root) {
    }

    public record Execution(
            int maxAttempts,
            int commandTimeoutSeconds
    ) {
    }

    public record Model(String provider) {
    }
}
