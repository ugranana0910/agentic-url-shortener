package com.navya.agentic_url_shortener.policy;

import lombok.Value;

@Value
public class PolicyCheckResult {

    String policy;
    boolean passed;
    String detail;
}