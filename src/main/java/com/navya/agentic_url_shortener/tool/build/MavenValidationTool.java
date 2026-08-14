package com.navya.agentic_url_shortener.tool.build;

import com.navya.agentic_url_shortener.config.AgenticProperties;
import com.navya.agentic_url_shortener.tool.repository.RepositoryAccessException;
import com.navya.agentic_url_shortener.tool.repository.SafeRepositoryResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class MavenValidationTool {

    private final SafeRepositoryResolver repositoryResolver;
    private final AgenticProperties properties;

    public BuildExecutionResult runTests(
            String repositoryPath
    ) {
        Path repository =
                repositoryResolver.resolve(repositoryPath);

        List<String> command =
                createCommand(repository);

        ProcessBuilder processBuilder =
                new ProcessBuilder(command);

        processBuilder.directory(repository.toFile());
        processBuilder.redirectErrorStream(true);

        Instant startedAt = Instant.now();

        try {
            Process process = processBuilder.start();

            /*
             * Read asynchronously so a full process pipe cannot deadlock
             * while the main thread waits for completion.
             */
            var outputFuture =
                    java.util.concurrent.CompletableFuture
                            .supplyAsync(
                                    () -> readOutput(process)
                            );

            boolean completed = process.waitFor(
                    properties.getExecution()
                            .getCommandTimeoutSeconds(),
                    TimeUnit.SECONDS
            );

            if (!completed) {
                process.destroy();

                if (!process.waitFor(
                        5,
                        TimeUnit.SECONDS
                )) {
                    process.destroyForcibly();
                }
            }

            String completeOutput =
                    outputFuture.get(
                            10,
                            TimeUnit.SECONDS
                    );

            int exitCode = completed
                    ? process.exitValue()
                    : -1;

            OutputLimit limited = limitOutput(
                    completeOutput
            );

            return new BuildExecutionResult(
                    List.copyOf(command),
                    exitCode,
                    !completed,
                    Duration.between(
                            startedAt,
                            Instant.now()
                    ),
                    limited.value(),
                    limited.truncated()
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new BuildExecutionException(
                    "Validation execution was interrupted",
                    exception
            );
        } catch (Exception exception) {
            throw new BuildExecutionException(
                    "Unable to execute Maven validation",
                    exception
            );
        }
    }

    private List<String> createCommand(Path repository) {
        boolean windows = System.getProperty("os.name")
                .toLowerCase(Locale.ROOT)
                .contains("win");

        Path wrapper = repository.resolve(
                windows ? "mvnw.cmd" : "mvnw"
        );

        if (!Files.isRegularFile(wrapper)) {
            throw new RepositoryAccessException(
                    "Maven Wrapper was not found in repository"
            );
        }

        List<String> command = new ArrayList<>();

        if (windows) {
            command.add("cmd.exe");
            command.add("/d");
            command.add("/c");
            command.add(wrapper.toString());
        } else {
            if (!Files.isExecutable(wrapper)) {
                throw new RepositoryAccessException(
                        "Maven Wrapper is not executable"
                );
            }

            command.add(wrapper.toString());
        }

        command.add("--batch-mode");
        command.add("--no-transfer-progress");
        command.add("test");

        return command;
    }

    private String readOutput(Process process) {
        try {
            return new String(
                    process.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );
        } catch (IOException exception) {
            throw new BuildExecutionException(
                    "Unable to capture validation output",
                    exception
            );
        }
    }

    private OutputLimit limitOutput(String output) {
        int maximum = properties.getExecution()
                .getMaxOutputCharacters();

        if (output.length() <= maximum) {
            return new OutputLimit(output, false);
        }

        return new OutputLimit(
                output.substring(0, maximum)
                        + System.lineSeparator()
                        + "[output truncated]",
                true
        );
    }

    private record OutputLimit(
            String value,
            boolean truncated
    ) {
    }
}