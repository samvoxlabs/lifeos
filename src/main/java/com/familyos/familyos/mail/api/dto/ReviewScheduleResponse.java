package com.familyos.familyos.mail.api.dto;

import java.util.List;

public record ReviewScheduleResponse(
    String mailbox,
    String reviewedAt,
    int messagesAnalyzed,
    int messagesFiltered,
    List<MailEventResponse> events,
    List<MailConflictResponse> conflicts
) {}
