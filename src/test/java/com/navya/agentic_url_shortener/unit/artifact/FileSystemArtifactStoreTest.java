package com.navya.agentic_url_shortener.unit.artifact;

import com.navya.agentic_url_shortener.artifact.ArtifactType;
import com.navya.agentic_url_shortener.artifact.FileSystemArtifactStore;
import com.navya.agentic_url_shortener.config.AgenticProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileSystemArtifactStoreTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void writesHashedVersionedArtifact()
            throws Exception {
        AgenticProperties properties =
                new AgenticProperties();

        properties.getWorkspace()
                .setRoot(temporaryDirectory);

        FileSystemArtifactStore store =
                new FileSystemArtifactStore(
                        properties,
                        Clock.fixed(
                                Instant.parse(
                                        "2026-08-13T12:00:00Z"
                                ),
                                ZoneOffset.UTC
                        )
                );

        UUID workflowId = UUID.randomUUID();

        var artifact = store.write(
                workflowId,
                1,
                UUID.randomUUID(),
                ArtifactType.ARCHITECTURE,
                "architecture.md",
                "# Architecture"
        );

        assertThat(artifact.getSha256()).hasSize(64);
        assertThat(artifact.getWorkflowRevision())
                .isEqualTo(1);

        assertThat(
                Files.exists(
                        temporaryDirectory.resolve(
                                artifact.getRelativePath()
                        )
                )
        ).isTrue();

        assertThat(store.findByWorkflowId(workflowId))
                .containsExactly(artifact);
    }

    @Test
    void rejectsUnsafeArtifactName() {
        AgenticProperties properties =
                new AgenticProperties();

        properties.getWorkspace()
                .setRoot(temporaryDirectory);

        FileSystemArtifactStore store =
                new FileSystemArtifactStore(
                        properties,
                        Clock.systemUTC()
                );

        assertThatThrownBy(
                () -> store.write(
                        UUID.randomUUID(),
                        1,
                        UUID.randomUUID(),
                        ArtifactType.ARCHITECTURE,
                        "../architecture.md",
                        "content"
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Artifact name contains unsupported characters"
                );
    }
}