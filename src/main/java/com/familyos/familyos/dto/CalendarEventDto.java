package com.familyos.familyos.dto;

public record CalendarEventDto(
        String id,
        String summary,
        String location,
        String description,
        String status,
        String start,
        String end
) {}
