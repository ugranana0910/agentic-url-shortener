package com.navya.agentic_url_shortener.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Path;

@Data
@Validated
@ConfigurationProperties(prefix = "agentic")
public class AgenticProperties {

    @Valid
    @NotNull
    private Workspace workspace = new Workspace();

    @Valid
    @NotNull
    private Repository repository = new Repository();

    @Valid
    @NotNull
    private Execution execution = new Execution();

    @Valid
    @NotNull
    private Model model = new Model();

    @Data
    public static class Workspace {

        @NotNull
        private Path root = Path.of("./agent-workspaces");
    }

    @Data
    public static class Repository {

        @NotNull
        private Path allowedRoot = Path.of(".");

        @Min(1)
        @Max(100_000)
        private int maxFiles = 2000;

        @Min(1024)
        private long maxFileSizeBytes = 1_048_576;
    }

    @Data
    public static class Execution {

        @Min(1)
        @Max(10)
        private int maxAttempts = 2;

        @Min(1)
        private int commandTimeoutSeconds = 120;
    }

    @Data
    public static class Model {

        @NotBlank
        private String provider = "deterministic";
    }
}