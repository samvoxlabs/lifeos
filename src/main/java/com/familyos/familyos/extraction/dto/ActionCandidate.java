package com.familyos.familyos.extraction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ActionCandidate(
    @JsonProperty("type")
    String type,
    
    @JsonProperty("title")
    String title,
    
    @JsonProperty("description")
    String description,
    
    @JsonProperty("dueDate")
    String dueDate
) {
    public enum ActionType {
        EVENT,
        TASK,
        REMINDER
    }
}
