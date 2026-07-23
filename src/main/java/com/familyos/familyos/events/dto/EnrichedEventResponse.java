package com.familyos.familyos.events.dto;

public record EnrichedEventResponse(
    String id,
    String title,
    String type,
    String category,
    String priority,
    String start,
    String end,
    String timezone,
    String location,
    String description,
    Double confidence,
    String status,
    String ownerId,
    EventSourceDto source
) {}
