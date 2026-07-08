package com.familyos.familyos.api.dto;

import java.util.List;

public record SearchResponse(
    String query,
    List<TaskResponse> tasks,
    List<EventResponse> events,
    List<ReminderResponse> reminders
) {}
