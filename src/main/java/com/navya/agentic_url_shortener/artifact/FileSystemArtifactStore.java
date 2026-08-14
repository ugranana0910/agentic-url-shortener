package com.navya.agentic_url_shortener.artifact;

import com.navya.agentic_url_shortener.config.AgenticProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class FileSystemArtifactStore
        implements ArtifactStore {

    private static final Pattern SAFE_NAME =
            Pattern.compile("[A-Za-z0-9._-]+");

    private final AgenticProperties properties;
    private final Clock clock;

    private final ConcurrentMap<UUID, List<ArtifactReference>>
            catalog = new ConcurrentHashMap<>();

    @Override
    public ArtifactReference write(
            UUID workflowId,
            int workflowRevision,
            UUID taskId,
            ArtifactType type,
            String name,
            String content
    ) {
        validateName(name);

        try {
            Path root = properties.getWorkspace()
                    .getRoot()
                    .toAbsolutePath()
                    .normalize();

            Path artifactDirectory = root
                    .resolve(workflowId.toString())
                    .resolve("revision-" + workflowRevision)
                    .resolve("artifacts")
                    .normalize();

            if (!artifactDirectory.startsWith(root)) {
                throw new IllegalStateException(
                        "Artifact path escaped workspace root"
                );
            }

            Files.createDirectories(artifactDirectory);

            byte[] bytes = content.getBytes(
                    StandardCharsets.UTF_8
            );

            Path artifactFile =
                    artifactDirectory.resolve(name)
                            .normalize();

            if (!artifactFile.startsWith(
                    artifactDirectory
            )) {
                throw new IllegalStateException(
                        "Artifact path escaped artifact directory"
                );
            }

            Files.write(
                    artifactFile,
                    bytes,
                    StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE
            );

            ArtifactReference reference =
                    new ArtifactReference(
                            UUID.randomUUID(),
                            workflowId,
                            workflowRevision,
                            taskId,
                            type,
                            name,
                            root.relativize(artifactFile)
                                    .toString()
                                    .replace('\\', '/'),
                            sha256(bytes),
                            bytes.length,
                            clock.instant()
                    );

            catalog.compute(
                    workflowId,
                    (ignored, existing) -> {
                        List<ArtifactReference> updated =
                                existing == null
                                        ? new ArrayList<>()
                                        : new ArrayList<>(existing);

                        updated.add(reference);
                        return List.copyOf(updated);
                    }
            );

            return reference;
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to write engineering artifact",
                    exception
            );
        }
    }

    @Override
    public List<ArtifactReference> findByWorkflowId(
            UUID workflowId
    ) {
        return catalog.getOrDefault(
                        workflowId,
                        List.of()
                )
                .stream()
                .sorted(
                        Comparator.comparing(
                                ArtifactReference::getCreatedAt
                        )
                )
                .toList();
    }

    private void validateName(String name) {
        if (name == null ||
                !SAFE_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException(
                    "Artifact name contains unsupported characters"
            );
        }
    }

    private String sha256(byte[] bytes) {
        try {
            byte[] digest =
                    MessageDigest.getInstance("SHA-256")
                            .digest(bytes);

            return java.util.HexFormat.of()
                    .formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is unavailable",
                    exception
            );
        }
    }
}