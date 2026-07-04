package com.familyos.familyos.integrations.google.calendar;

public record GoogleCalendarEvent(
        String id,
        String summary,
        String location,
        String description,
        String status,
        String start,
        String end
) {}
