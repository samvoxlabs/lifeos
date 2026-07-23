package com.familyos.familyos.mail.api.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record MailSyncResponse(
        String mailbox,
        OffsetDateTime syncedAt,
        String nextCursor,
        List<MailMessageResponse> messages,
        List<MailEventResponse> events,
        List<MailConflictResponse> conflicts
) {
}
