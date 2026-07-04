package com.familyos.familyos.domain.dto;

import java.util.UUID;

public record ExtractionDto(
    UUID id,
    String summary,
    Double confidence,
    String model,
    String provider,
    String promptVersion
) {}
