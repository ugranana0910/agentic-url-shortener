package com.navya.agentic_url_shortener.orchestration.engine;

import com.navya.agentic_url_shortener.orchestration.domain.TaskType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class WorkflowTaskHandlerRegistry {

    private final Map<TaskType, WorkflowTaskHandler> handlers =
            new EnumMap<>(TaskType.class);

    public WorkflowTaskHandlerRegistry(
            List<WorkflowTaskHandler> taskHandlers
    ) {
        for (WorkflowTaskHandler handler : taskHandlers) {
            WorkflowTaskHandler previous =
                    handlers.put(
                            handler.supports(),
                            handler
                    );

            if (previous != null) {
                throw new IllegalStateException(
                        "Multiple handlers registered for "
                                + handler.supports()
                );
            }
        }
    }

    public WorkflowTaskHandler require(TaskType taskType) {
        WorkflowTaskHandler handler =
                handlers.get(taskType);

        if (handler == null) {
            throw new IllegalStateException(
                    "No task handler registered for "
                            + taskType
            );
        }

        return handler;
    }
}