package com.familyos.familyos.calendar.dto;

import java.util.List;

public record CalendarResetResponse(
        String status,
        int eventsReset,
        List<String> resetEventIds,
        String resetAt
) {}
