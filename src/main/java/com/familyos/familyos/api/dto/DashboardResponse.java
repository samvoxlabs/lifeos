package com.familyos.familyos.api.dto;

import java.util.List;

public record DashboardResponse(
    Summary summary,
    List<TaskSummary> recentTasks,
    List<EventResponse> upcomingEvents,
    List<ReminderResponse> activeReminders
) {
    public record Summary(
        long pendingTasks,
        long upcomingEvents,
        long activeReminders
    ) {}
}
