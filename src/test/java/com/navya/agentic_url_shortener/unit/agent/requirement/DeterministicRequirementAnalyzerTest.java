package com.navya.agentic_url_shortener.unit.agent.requirement;

import com.navya.agentic_url_shortener.agent.requirement.DeterministicRequirementAnalyzer;
import com.navya.agentic_url_shortener.agent.requirement.RiskLevel;
import com.navya.agentic_url_shortener.orchestration.domain.ScenarioType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeterministicRequirementAnalyzerTest {

    private final DeterministicRequirementAnalyzer analyzer =
            new DeterministicRequirementAnalyzer();

    @Test
    void normalizesRequirementAndCreatesCriteria() {
        var result = analyzer.analyze(
                "  Add   redirect analytics to the URL API  ",
                ScenarioType.BROWNFIELD
        );

        assertThat(result.getNormalizedRequirement())
                .isEqualTo(
                        "Add redirect analytics to the URL API"
                );

        assertThat(result.getAcceptanceCriteria())
                .anyMatch(criteria ->
                        criteria.contains("Analytics")
                );

        assertThat(result.getRiskLevel())
                .isEqualTo(RiskLevel.MEDIUM);
    }

    @Test
    void detectsAmbiguousRequirement() {
        var result = analyzer.analyze(
                "Make shortened URLs safer and better",
                ScenarioType.AMBIGUOUS
        );

        assertThat(result.requiresClarification())
                .isTrue();

        assertThat(result.getAmbiguities())
                .isNotEmpty();
    }

    @Test
    void detectsDocumentationOnlyRequirement() {
        var result = analyzer.analyze(
                "Update the README setup instructions",
                ScenarioType.BROWNFIELD
        );

        assertThat(result.isDocumentationOnly())
                .isTrue();
    }

    @Test
    void classifiesSecurityRequirementAsHighRisk() {
        var result = analyzer.analyze(
                "Add authentication to the public URL API",
                ScenarioType.GREENFIELD
        );

        assertThat(result.getRiskLevel())
                .isEqualTo(RiskLevel.HIGH);
    }

    @Test
    void documentationOnlyBrownfieldRequestIsNotAmbiguous() {
        var result = analyzer.analyze(
                "Update the README setup instructions",
                ScenarioType.BROWNFIELD
        );

        assertThat(result.isDocumentationOnly()).isTrue();
        assertThat(result.requiresClarification()).isFalse();
        assertThat(result.getAmbiguities()).isEmpty();
    }
}