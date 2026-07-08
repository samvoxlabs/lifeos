package com.familyos.familyos.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TaskResponse(
    UUID id,
    String title,
    String description,
    String status,
    String priority,
    Double confidence,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
