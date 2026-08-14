package com.navya.agentic_url_shortener.tool.repository;

import lombok.Value;

import java.util.List;

@Value
public class RepositoryAnalysis {

    String repositoryRoot;
    String buildSystem;
    List<String> sourceFiles;
    List<String> testFiles;
    List<String> migrationFiles;
    List<String> configurationFiles;
    List<String> documentationFiles;
    List<String> impactedFiles;
    List<String> detectedModules;
    int inspectedFileCount;
    boolean truncated;
}