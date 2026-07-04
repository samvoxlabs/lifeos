package com.familyos.familyos.domain.dto;

import com.familyos.familyos.extraction.dto.ExtractionResult;
import com.familyos.familyos.ruleengine.dto.NormalizedDocument;

public record ProcessExtractionRequest(
    NormalizedDocument sourceDocument,
    ExtractionResult extractionResult
) {}
