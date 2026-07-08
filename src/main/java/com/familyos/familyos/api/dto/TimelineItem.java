package com.familyos.familyos.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TimelineItem(
    UUID id,
    String type,
    String title,
    String description,
    String status,
    LocalDateTime date
) {}
