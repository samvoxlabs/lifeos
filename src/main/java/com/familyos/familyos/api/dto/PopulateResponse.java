package com.familyos.familyos.api.dto;

public record PopulateResponse(
    String mode,
    String status,
    int seedDocumentsImported,
    SyncSummaryResponse summary,
    String message
) {}
