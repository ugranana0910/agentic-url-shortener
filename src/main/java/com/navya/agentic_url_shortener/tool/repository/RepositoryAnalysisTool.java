package com.navya.agentic_url_shortener.tool.repository;

import com.navya.agentic_url_shortener.config.AgenticProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class RepositoryAnalysisTool {

    private static final Set<String> IGNORED_DIRECTORIES =
            Set.of(
                    ".git",
                    ".idea",
                    "target",
                    "build",
                    "out",
                    "node_modules",
                    "agent-workspaces"
            );

    private static final Set<String> TEXT_EXTENSIONS =
            Set.of(
                    ".java",
                    ".kt",
                    ".xml",
                    ".yml",
                    ".yaml",
                    ".properties",
                    ".sql",
                    ".md",
                    ".json"
            );

    private final SafeRepositoryResolver repositoryResolver;
    private final AgenticProperties properties;

    public RepositoryAnalysis analyze(
            String repositoryPath,
            String requirement
    ) {
        Path repositoryRoot =
                repositoryResolver.resolve(repositoryPath);

        int maxFiles =
                properties.getRepository().getMaxFiles();

        List<Path> files = collectFiles(
                repositoryRoot,
                maxFiles + 1
        );

        boolean truncated = files.size() > maxFiles;

        if (truncated) {
            files = new ArrayList<>(
                    files.subList(0, maxFiles)
            );
        }

        List<String> sourceFiles = new ArrayList<>();
        List<String> testFiles = new ArrayList<>();
        List<String> migrationFiles = new ArrayList<>();
        List<String> configurationFiles =
                new ArrayList<>();
        List<String> documentationFiles =
                new ArrayList<>();

        for (Path file : files) {
            String relative = relative(
                    repositoryRoot,
                    file
            );

            String normalized =
                    relative.replace('\\', '/');

            String lowercase =
                    normalized.toLowerCase(Locale.ROOT);

            if (lowercase.contains("/src/test/") ||
                    lowercase.startsWith("src/test/")) {
                testFiles.add(normalized);
            } else if (lowercase.contains("/src/main/") ||
                    lowercase.startsWith("src/main/")) {
                sourceFiles.add(normalized);
            }

            if (lowercase.contains("/db/migration/") ||
                    lowercase.startsWith(
                            "src/main/resources/db/migration/"
                    )) {
                migrationFiles.add(normalized);
            }

            if (isConfigurationFile(lowercase)) {
                configurationFiles.add(normalized);
            }

            if (lowercase.endsWith(".md")) {
                documentationFiles.add(normalized);
            }
        }

        List<String> impactedFiles =
                findImpactedFiles(
                        repositoryRoot,
                        files,
                        requirement
                );

        return new RepositoryAnalysis(
                repositoryRoot.toString(),
                detectBuildSystem(repositoryRoot),
                List.copyOf(sourceFiles),
                List.copyOf(testFiles),
                List.copyOf(migrationFiles),
                List.copyOf(configurationFiles),
                List.copyOf(documentationFiles),
                impactedFiles,
                detectModules(repositoryRoot),
                files.size(),
                truncated
        );
    }

    private List<Path> collectFiles(
            Path repositoryRoot,
            int limit
    ) {
        try (Stream<Path> stream =
                     Files.walk(repositoryRoot)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path ->
                            !containsIgnoredDirectory(
                                    repositoryRoot,
                                    path
                            )
                    )
                    .filter(this::isSupportedTextFile)
                    .sorted(
                            Comparator.comparing(
                                    path -> relative(
                                            repositoryRoot,
                                            path
                                    )
                            )
                    )
                    .limit(limit)
                    .toList();
        } catch (IOException exception) {
            throw new RepositoryAccessException(
                    "Unable to inspect repository",
                    exception
            );
        }
    }

    private boolean containsIgnoredDirectory(
            Path repositoryRoot,
            Path path
    ) {
        Path relative =
                repositoryRoot.relativize(path);

        for (Path part : relative) {
            if (IGNORED_DIRECTORIES.contains(
                    part.toString()
            )) {
                return true;
            }
        }

        return false;
    }

    private boolean isSupportedTextFile(Path path) {
        String name = path.getFileName()
                .toString()
                .toLowerCase(Locale.ROOT);

        return TEXT_EXTENSIONS.stream()
                .anyMatch(name::endsWith);
    }

    private boolean isConfigurationFile(
            String lowercase
    ) {
        return lowercase.endsWith(".yml") ||
                lowercase.endsWith(".yaml") ||
                lowercase.endsWith(".properties") ||
                lowercase.endsWith("pom.xml") ||
                lowercase.endsWith("docker-compose.yml") ||
                lowercase.endsWith("docker-compose.yaml");
    }

    private List<String> findImpactedFiles(
            Path repositoryRoot,
            List<Path> files,
            String requirement
    ) {
        Set<String> terms =
                significantTerms(requirement);

        LinkedHashSet<String> impacted =
                new LinkedHashSet<>();

        for (Path file : files) {
            if (impacted.size() >= 50) {
                break;
            }

            if (fileMatchesTerms(file, terms)) {
                impacted.add(
                        relative(repositoryRoot, file)
                                .replace('\\', '/')
                );
            }
        }

        return List.copyOf(impacted);
    }

    private boolean fileMatchesTerms(
            Path file,
            Set<String> terms
    ) {
        if (terms.isEmpty()) {
            return false;
        }

        try {
            long size = Files.size(file);

            if (size >
                    properties.getRepository()
                            .getMaxFileSizeBytes()) {
                return false;
            }

            String filename = file.getFileName()
                    .toString()
                    .toLowerCase(Locale.ROOT);

            for (String term : terms) {
                if (filename.contains(term)) {
                    return true;
                }
            }

            String content = Files.readString(
                    file,
                    StandardCharsets.UTF_8
            ).toLowerCase(Locale.ROOT);

            return terms.stream().anyMatch(content::contains);
        } catch (IOException |
                 RuntimeException exception) {
            return false;
        }
    }

    private Set<String> significantTerms(
            String requirement
    ) {
        if (requirement == null) {
            return Set.of();
        }

        Set<String> ignored =
                Set.of(
                        "add",
                        "the",
                        "and",
                        "with",
                        "from",
                        "into",
                        "existing",
                        "current",
                        "change",
                        "create",
                        "update"
                );

        LinkedHashSet<String> result =
                new LinkedHashSet<>();

        for (String term :
                requirement.toLowerCase(Locale.ROOT)
                        .split("[^a-z0-9]+")) {
            if (term.length() >= 3 &&
                    !ignored.contains(term)) {
                result.add(term);
            }
        }

        return result;
    }

    private String detectBuildSystem(Path root) {
        if (Files.exists(root.resolve("pom.xml"))) {
            return "MAVEN";
        }

        if (Files.exists(root.resolve("build.gradle")) ||
                Files.exists(
                        root.resolve("build.gradle.kts")
                )) {
            return "GRADLE";
        }

        if (Files.exists(root.resolve("package.json"))) {
            return "NODE";
        }

        return "UNKNOWN";
    }

    private List<String> detectModules(Path root) {
        List<String> modules = new ArrayList<>();

        try (Stream<Path> children = Files.list(root)) {
            children.filter(Files::isDirectory)
                    .filter(directory ->
                            Files.exists(
                                    directory.resolve("pom.xml")
                            ) ||
                                    Files.exists(
                                            directory.resolve(
                                                    "build.gradle"
                                            )
                                    ) ||
                                    Files.exists(
                                            directory.resolve(
                                                    "build.gradle.kts"
                                            )
                                    )
                    )
                    .map(directory ->
                            directory.getFileName()
                                    .toString()
                    )
                    .sorted()
                    .forEach(modules::add);
        } catch (IOException exception) {
            throw new RepositoryAccessException(
                    "Unable to detect repository modules",
                    exception
            );
        }

        if (modules.isEmpty()) {
            modules.add(".");
        }

        return List.copyOf(modules);
    }

    private String relative(
            Path repositoryRoot,
            Path path
    ) {
        return repositoryRoot
                .relativize(path)
                .toString();
    }
}