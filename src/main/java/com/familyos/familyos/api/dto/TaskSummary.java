package com.familyos.familyos.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TaskSummary(
    UUID id,
    String title,
    String status,
    LocalDateTime createdAt
) {}
