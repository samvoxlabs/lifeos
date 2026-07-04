package com.familyos.familyos.extraction.dto;

import com.familyos.familyos.ruleengine.dto.NormalizedDocument;
import com.familyos.familyos.ruleengine.dto.RuleResult;

public record ExtractionResponse(
    String status,
    ExtractionResult result,
    RuleResult ruleResult,
    String message
) {
    public static ExtractionResponse success(ExtractionResult result) {
        return new ExtractionResponse("SUCCESS", result, null, null);
    }
    
    public static ExtractionResponse skipped(RuleResult ruleResult) {
        return new ExtractionResponse("SKIPPED", null, ruleResult, 
            "Document skipped by Rule Engine: " + ruleResult.decision());
    }
    
    public static ExtractionResponse error(String message) {
        return new ExtractionResponse("ERROR", null, null, message);
    }
}
