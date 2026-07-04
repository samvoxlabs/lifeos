package com.familyos.familyos.extraction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ExtractionResult(
    @JsonProperty("summary")
    String summary,
    
    @JsonProperty("confidence")
    Double confidence,
    
    @JsonProperty("actions")
    List<ActionCandidate> actions
) {}
