package com.familyos.familyos.ruleengine.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public record NormalizedDocument(
    @JsonProperty("id") String id,
    @JsonProperty("sender") String sender,
    @JsonProperty("subject") String subject,
    @JsonProperty("content") String content,
    @JsonProperty("labels") List<String> labels,
    @JsonProperty("priority") String priority,
    @JsonProperty("source") String source,
    @JsonProperty("provider") String provider,
    @JsonProperty("sourceType") String sourceType,
    @JsonProperty("externalId") String externalId,
    @JsonProperty("rawContent") String rawContent,
    @JsonProperty("metadata") Map<String, Object> metadata
) {
    public NormalizedDocument(
        String id,
        String sender,
        String subject,
        String content,
        List<String> labels,
        String priority,
        String source
    ) {
        this(id, sender, subject, content, labels, priority, source, null, null, null, null, Map.of());
    }
}
