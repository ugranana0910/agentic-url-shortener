package com.navya.agentic_url_shortener.unit.tool.repository;

import com.navya.agentic_url_shortener.config.AgenticProperties;
import com.navya.agentic_url_shortener.tool.repository.RepositoryAccessException;
import com.navya.agentic_url_shortener.tool.repository.SafeRepositoryResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SafeRepositoryResolverTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void resolvesDirectoryInsideAllowedRoot()
            throws Exception {
        Path repository =
                Files.createDirectory(
                        temporaryDirectory.resolve("repository")
                );

        SafeRepositoryResolver resolver =
                new SafeRepositoryResolver(
                        properties(temporaryDirectory)
                );

        assertThat(resolver.resolve("repository"))
                .isEqualTo(repository.toRealPath());
    }

    @Test
    void rejectsPathOutsideAllowedRoot() {
        SafeRepositoryResolver resolver =
                new SafeRepositoryResolver(
                        properties(temporaryDirectory)
                );

        assertThatThrownBy(
                () -> resolver.resolve("../outside")
        )
                .isInstanceOf(
                        RepositoryAccessException.class
                )
                .hasMessage(
                        "Repository path is outside the approved root"
                );
    }

    @Test
    void rejectsMissingRepository() {
        SafeRepositoryResolver resolver =
                new SafeRepositoryResolver(
                        properties(temporaryDirectory)
                );

        assertThatThrownBy(
                () -> resolver.resolve("missing")
        )
                .isInstanceOf(
                        RepositoryAccessException.class
                )
                .hasMessageContaining(
                        "Repository path does not exist"
                );
    }

    private AgenticProperties properties(Path root) {
        AgenticProperties properties =
                new AgenticProperties();

        properties.getRepository()
                .setAllowedRoot(root);

        return properties;
    }
}