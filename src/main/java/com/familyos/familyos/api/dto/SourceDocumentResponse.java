package com.familyos.familyos.api.dto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record SourceDocumentResponse(
    UUID id,
    String provider,
    String sourceType,
    String externalId,
    String subject,
    String rawContent,
    Map<String, Object> metadata,
    String processingStatus,
    LocalDateTime receivedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
