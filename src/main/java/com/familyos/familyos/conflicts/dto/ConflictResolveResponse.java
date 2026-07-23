package com.familyos.familyos.conflicts.dto;

public record ConflictResolveResponse(
    String conflictId,
    String status,
    String selectedOptionId,
    String resolvedAt
) {}
