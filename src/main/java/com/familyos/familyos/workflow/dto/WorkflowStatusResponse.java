package com.familyos.familyos.workflow.dto;

public record WorkflowStatusResponse(
    String workflowId,
    String status,
    String currentStep,
    int emailsProcessed,
    int eventsExtracted,
    int conflictsDetected,
    String errorMessage,
    String startedAt,
    String completedAt
) {}
