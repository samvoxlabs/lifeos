package com.familyos.familyos.ruleengine.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record NormalizedDocument(
    @JsonProperty("id") String id,
    @JsonProperty("sender") String sender,
    @JsonProperty("subject") String subject,
    @JsonProperty("content") String content,
    @JsonProperty("labels") List<String> labels,
    @JsonProperty("priority") String priority,
    @JsonProperty("source") String source
) {
}
