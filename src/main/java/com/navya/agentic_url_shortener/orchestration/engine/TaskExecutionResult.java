package com.navya.agentic_url_shortener.orchestration.engine;

import lombok.Value;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Value
public class TaskExecutionResult {

    Map<String, Object> contextUpdates;

    public TaskExecutionResult(
            Map<String, Object> contextUpdates
    ) {
        this.contextUpdates =
                contextUpdates == null
                        ? Collections.emptyMap()
                        : Collections.unmodifiableMap(
                        new LinkedHashMap<>(
                                contextUpdates
                        )
                );
    }

    public static TaskExecutionResult empty() {
        return new TaskExecutionResult(
                Collections.emptyMap()
        );
    }

    public static TaskExecutionResult of(
            String key,
            Object value
    ) {
        return new TaskExecutionResult(
                Map.of(key, value)
        );
    }
}