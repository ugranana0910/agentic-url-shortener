package com.navya.agentic_url_shortener.agent.requirement;

import com.navya.agentic_url_shortener.orchestration.domain.ScenarioType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class DeterministicRequirementAnalyzer
        implements RequirementAnalyzer {

    private static final List<String> AMBIGUOUS_TERMS =
            List.of(
                    "better",
                    "safer",
                    "faster",
                    "improve",
                    "optimize",
                    "permanent",
                    "user-friendly",
                    "production-ready"
            );

    private static final List<String> HIGH_RISK_TERMS =
            List.of(
                    "authentication",
                    "authorization",
                    "security",
                    "secret",
                    "credential",
                    "payment",
                    "delete",
                    "migration",
                    "public api"
            );

    @Override
    public RequirementAnalysis analyze(
            String requirement,
            ScenarioType scenarioType
    ) {
        String normalized = normalize(requirement);

        String lowercase =
                normalized.toLowerCase(Locale.ROOT);

        boolean documentationOnly =
                isDocumentationOnly(lowercase);

        List<String> ambiguities =
                detectAmbiguities(
                        lowercase,
                        scenarioType,
                        documentationOnly
                );

        List<String> acceptanceCriteria =
                createAcceptanceCriteria(
                        normalized,
                        lowercase,
                        documentationOnly
                );

        List<String> assumptions =
                createAssumptions(
                        scenarioType,
                        documentationOnly
                );

        RiskLevel riskLevel =
                classifyRisk(lowercase);

        return new RequirementAnalysis(
                normalized,
                List.copyOf(acceptanceCriteria),
                List.copyOf(ambiguities),
                List.copyOf(assumptions),
                riskLevel,
                documentationOnly
        );
    }

    private String normalize(String requirement) {
        if (requirement == null || requirement.isBlank()) {
            throw new IllegalArgumentException(
                    "requirement must not be blank"
            );
        }

        return requirement
                .trim()
                .replaceAll("\\s+", " ");
    }

    private List<String> detectAmbiguities(
            String lowercase,
            ScenarioType scenarioType,
            boolean documentationOnly
    ) {
        List<String> ambiguities = new ArrayList<>();

        for (String term : AMBIGUOUS_TERMS) {
            if (lowercase.contains(term)) {
                ambiguities.add(
                        "Define the measurable meaning of '"
                                + term
                                + "'"
                );
            }
        }

        /*
         * A documentation-only request already identifies its target through
         * README/docs terminology. It does not need to identify an application
         * module before documentation analysis can begin.
         */
        if (!documentationOnly &&
                scenarioType == ScenarioType.BROWNFIELD &&
                !mentionsExistingComponent(lowercase)) {
            ambiguities.add(
                    "Identify the existing component or behavior to change"
            );
        }

        if (scenarioType == ScenarioType.AMBIGUOUS &&
                ambiguities.isEmpty()) {
            ambiguities.add(
                    "Clarify expected behavior and measurable acceptance criteria"
            );
        }

        return ambiguities;
    }

    private boolean mentionsExistingComponent(
            String lowercase
    ) {
        return lowercase.contains("existing") ||
                lowercase.contains("current") ||
                lowercase.contains("url") ||
                lowercase.contains("api") ||
                lowercase.contains("controller") ||
                lowercase.contains("service") ||
                lowercase.contains("repository") ||
                lowercase.contains("database");
    }

    private boolean isDocumentationOnly(String lowercase) {
        boolean documentationTerm =
                lowercase.contains("documentation") ||
                        lowercase.contains("readme") ||
                        lowercase.contains("docs");

        boolean implementationTerm =
                lowercase.contains("implement") ||
                        lowercase.contains("endpoint") ||
                        lowercase.contains("database") ||
                        lowercase.contains("api") ||
                        lowercase.contains("code");

        return documentationTerm && !implementationTerm;
    }

    private List<String> createAcceptanceCriteria(
            String normalized,
            String lowercase,
            boolean documentationOnly
    ) {
        List<String> criteria = new ArrayList<>();

        criteria.add(
                "The requested outcome is delivered: "
                        + normalized
        );

        if (documentationOnly) {
            criteria.add(
                    "Documentation accurately reflects current behavior"
            );

            criteria.add(
                    "All documented setup and verification commands are reproducible"
            );

            return criteria;
        }

        criteria.add(
                "Existing automated tests continue to pass"
        );

        criteria.add(
                "New behavior is covered by automated tests"
        );

        if (lowercase.contains("api") ||
                lowercase.contains("endpoint")) {
            criteria.add(
                    "API responses and failure behavior are documented"
            );
        }

        if (lowercase.contains("analytics")) {
            criteria.add(
                    "Analytics updates do not change redirect behavior"
            );
        }

        if (lowercase.contains("idempot")) {
            criteria.add(
                    "Equivalent retries do not create duplicate resources"
            );
        }

        return criteria;
    }

    private List<String> createAssumptions(
            ScenarioType scenarioType,
            boolean documentationOnly
    ) {
        List<String> assumptions = new ArrayList<>();

        assumptions.add(
                "Changes remain within the approved repository workspace"
        );

        if (scenarioType == ScenarioType.BROWNFIELD) {
            assumptions.add(
                    "Existing public behavior remains backward compatible unless explicitly approved"
            );
        }

        if (!documentationOnly) {
            assumptions.add(
                    "Compilation and automated tests are mandatory exit gates"
            );
        }

        return assumptions;
    }

    private RiskLevel classifyRisk(String lowercase) {
        boolean highRisk = HIGH_RISK_TERMS.stream()
                .anyMatch(lowercase::contains);

        if (highRisk) {
            return RiskLevel.HIGH;
        }

        if (lowercase.contains("api") ||
                lowercase.contains("database") ||
                lowercase.contains("refactor") ||
                lowercase.contains("analytics")) {
            return RiskLevel.MEDIUM;
        }

        return RiskLevel.LOW;
    }
}