package com.familyos.familyos.ruleengine.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RuleResult(
    @JsonProperty("decision") RuleDecision decision,
    @JsonProperty("matchedRule") String matchedRule,
    @JsonProperty("reason") String reason,
    @JsonProperty("priorityScore") int priorityScore
) {
}
