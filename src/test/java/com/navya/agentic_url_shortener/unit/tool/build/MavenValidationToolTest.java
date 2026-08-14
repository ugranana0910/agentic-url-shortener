package com.navya.agentic_url_shortener.unit.tool.build;

import com.navya.agentic_url_shortener.config.AgenticProperties;
import com.navya.agentic_url_shortener.tool.build.MavenValidationTool;
import com.navya.agentic_url_shortener.tool.repository.RepositoryAccessException;
import com.navya.agentic_url_shortener.tool.repository.SafeRepositoryResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MavenValidationToolTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void rejectsRepositoryWithoutMavenWrapper()
            throws Exception {
        Files.createDirectory(
                temporaryDirectory.resolve("repository")
        );

        AgenticProperties properties =
                new AgenticProperties();

        properties.getRepository()
                .setAllowedRoot(temporaryDirectory);

        MavenValidationTool tool =
                new MavenValidationTool(
                        new SafeRepositoryResolver(properties),
                        properties
                );

        assertThatThrownBy(
                () -> tool.runTests("repository")
        )
                .isInstanceOf(
                        RepositoryAccessException.class
                )
                .hasMessage(
                        "Maven Wrapper was not found in repository"
                );
    }
}