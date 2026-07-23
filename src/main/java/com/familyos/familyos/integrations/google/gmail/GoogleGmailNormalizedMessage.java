package com.familyos.familyos.integrations.google.gmail;

import java.time.OffsetDateTime;

public record GoogleGmailNormalizedMessage(
        String gmailMessageId,
        String threadId,
        String senderName,
        String senderEmail,
        java.util.List<String> to,
        String subject,
        String snippet,
        String bodyText,
        OffsetDateTime receivedAt,
        String historyId,
        boolean read,
        java.util.List<String> labels
) {
}
