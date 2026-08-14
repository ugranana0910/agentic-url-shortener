package com.navya.agentic_url_shortener.tool.repository;

import com.navya.agentic_url_shortener.config.AgenticProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@RequiredArgsConstructor
public class SafeRepositoryResolver {

    private final AgenticProperties properties;

    public Path resolve(String repositoryPath) {
        if (repositoryPath == null ||
                repositoryPath.isBlank()) {
            throw new RepositoryAccessException(
                    "repositoryPath is required"
            );
        }

        try {
            Path allowedRoot = properties
                    .getRepository()
                    .getAllowedRoot()
                    .toAbsolutePath()
                    .normalize()
                    .toRealPath();

            Path candidate = allowedRoot
                    .resolve(repositoryPath)
                    .normalize()
                    .toAbsolutePath();

            if (!candidate.startsWith(allowedRoot)) {
                throw new RepositoryAccessException(
                        "Repository path is outside the approved root"
                );
            }

            if (!Files.exists(candidate)) {
                throw new RepositoryAccessException(
                        "Repository path does not exist: "
                                + repositoryPath
                );
            }

            if (!Files.isDirectory(candidate)) {
                throw new RepositoryAccessException(
                        "Repository path is not a directory: "
                                + repositoryPath
                );
            }

            Path realCandidate = candidate.toRealPath();

            if (!realCandidate.startsWith(allowedRoot)) {
                throw new RepositoryAccessException(
                        "Repository resolves outside the approved root"
                );
            }

            return realCandidate;
        } catch (IOException exception) {
            throw new RepositoryAccessException(
                    "Unable to resolve repository path",
                    exception
            );
        }
    }
}