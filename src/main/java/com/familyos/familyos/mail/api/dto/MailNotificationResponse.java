package com.familyos.familyos.mail.api.dto;

public record MailNotificationResponse(
        String id,
        String recipientMemberId,
        String channel,
        String status,
        String message
) {
}
