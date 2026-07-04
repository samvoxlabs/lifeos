package com.familyos.familyos.integrations.google.tasks;

public record GoogleTaskItem(
        String id,
        String title,
        String notes,
        String status,
        String due,
        String updated
) {}
