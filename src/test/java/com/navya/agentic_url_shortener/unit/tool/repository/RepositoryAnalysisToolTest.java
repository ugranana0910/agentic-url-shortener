package com.navya.agentic_url_shortener.unit.tool.repository;

import com.navya.agentic_url_shortener.config.AgenticProperties;
import com.navya.agentic_url_shortener.tool.repository.RepositoryAnalysisTool;
import com.navya.agentic_url_shortener.tool.repository.SafeRepositoryResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class RepositoryAnalysisToolTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void identifiesBuildSourcesTestsAndImpactedFiles()
            throws Exception {
        Path repository =
                Files.createDirectory(
                        temporaryDirectory.resolve("repository")
                );

        Files.writeString(
                repository.resolve("pom.xml"),
                "<project/>"
        );

        Path source = repository.resolve(
                "src/main/java/example"
        );

        Path tests = repository.resolve(
                "src/test/java/example"
        );

        Files.createDirectories(source);
        Files.createDirectories(tests);

        Files.writeString(
                source.resolve("RedirectService.java"),
                "class RedirectService { void analytics() {} }"
        );

        Files.writeString(
                tests.resolve("RedirectServiceTest.java"),
                "class RedirectServiceTest {}"
        );

        AgenticProperties properties =
                new AgenticProperties();

        properties.getRepository()
                .setAllowedRoot(temporaryDirectory);

        RepositoryAnalysisTool tool =
                new RepositoryAnalysisTool(
                        new SafeRepositoryResolver(properties),
                        properties
                );

        var analysis = tool.analyze(
                "repository",
                "Add redirect analytics"
        );

        assertThat(analysis.getBuildSystem())
                .isEqualTo("MAVEN");

        assertThat(analysis.getSourceFiles())
                .contains(
                        "src/main/java/example/RedirectService.java"
                );

        assertThat(analysis.getTestFiles())
                .contains(
                        "src/test/java/example/RedirectServiceTest.java"
                );

        assertThat(analysis.getImpactedFiles())
                .contains(
                        "src/main/java/example/RedirectService.java"
                );
    }
}