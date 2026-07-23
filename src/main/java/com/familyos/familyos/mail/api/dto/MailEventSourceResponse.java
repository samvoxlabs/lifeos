package com.familyos.familyos.mail.api.dto;

public record MailEventSourceResponse(
        String type,
        String messageId,
        String sender
) {
}
