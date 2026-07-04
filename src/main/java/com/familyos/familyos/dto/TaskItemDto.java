package com.familyos.familyos.dto;

public record TaskItemDto(
        String id,
        String title,
        String notes,
        String status,
        String due,
        String updated
) {}
