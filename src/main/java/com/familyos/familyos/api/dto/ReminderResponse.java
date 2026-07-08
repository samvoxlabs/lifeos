package com.familyos.familyos.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReminderResponse(
    UUID id,
    String title,
    String description,
    String status,
    Double confidence,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
