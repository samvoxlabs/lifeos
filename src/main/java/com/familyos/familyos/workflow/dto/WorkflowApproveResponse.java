package com.familyos.familyos.workflow.dto;

public record WorkflowApproveResponse(
    String workflowId,
    String status,
    String conflictId,
    String selectedOptionId,
    boolean calendarUpdated,
    String resolvedAt
) {}
