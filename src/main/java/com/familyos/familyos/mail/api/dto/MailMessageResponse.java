package com.familyos.familyos.mail.api.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record MailMessageResponse(
        String id,
        String threadId,
        MailParticipantResponse from,
        List<String> to,
        String subject,
        String snippet,
        String bodyText,
        OffsetDateTime receivedAt,
        boolean read,
        List<String> labels,
        String processingStatus,
        String extractedEventId
) {
}
