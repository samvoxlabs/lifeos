package com.familyos.familyos.api.dto;

public record SyncSummaryResponse(
    String status,
    int documentsRead,
    int documentsImported,
    int documentsSkipped,
    int documentsProcessed,
    int tasksCreated,
    int eventsCreated,
    int remindersCreated,
    long processingTimeMs
) {}
