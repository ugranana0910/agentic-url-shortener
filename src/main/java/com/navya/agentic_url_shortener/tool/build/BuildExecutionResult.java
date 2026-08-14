package com.navya.agentic_url_shortener.tool.build;

import lombok.Value;

import java.time.Duration;
import java.util.List;

@Value
public class BuildExecutionResult {

    List<String> command;
    int exitCode;
    boolean timedOut;
    Duration duration;
    String output;
    boolean outputTruncated;

    public boolean isSuccessful() {
        return !timedOut && exitCode == 0;
    }
}