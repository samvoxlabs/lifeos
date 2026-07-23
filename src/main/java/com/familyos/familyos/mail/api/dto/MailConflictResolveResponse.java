package com.familyos.familyos.mail.api.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record MailConflictResolveResponse(
        String conflictId,
        String status,
        String selectedOptionId,
        List<MailEventResponse> updatedEvents,
        List<MailNotificationResponse> notifications,
        OffsetDateTime resolvedAt,
        boolean calendarUpdated
) {
}
