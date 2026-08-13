package com.navya.agentic_url_shortener.orchestration.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GateDefinition {

    private GateType type;
    private String requiredContextKey;

    public static GateDefinition none() {
        return new GateDefinition(
                GateType.NONE,
                null
        );
    }

    public static GateDefinition dependenciesSucceeded() {
        return new GateDefinition(
                GateType.DEPENDENCIES_SUCCEEDED,
                null
        );
    }

    public static GateDefinition contextKeyPresent(
            String contextKey
    ) {
        return new GateDefinition(
                GateType.CONTEXT_KEY_PRESENT,
                contextKey
        );
    }

    public static GateDefinition humanApproval() {
        return new GateDefinition(
                GateType.HUMAN_APPROVAL,
                null
        );
    }
}