package com.familyos.familyos.mail.api.dto;

public record MailEventResponse(
        String id,
        String title,
        String start,
        String end,
        String timezone,
        String location,
        String description,
        String ownerId,
        String status,
        MailEventSourceResponse source,
        double confidence
) {
}
