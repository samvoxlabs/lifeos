package com.familyos.familyos.domain.dto;

import java.util.UUID;
import java.time.LocalDateTime;

public record SourceDocumentDto(
    UUID id,
    String externalId,
    String provider,
    String sourceType,
    LocalDateTime receivedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String processingStatus
) {}
