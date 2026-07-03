package com.familyos.familyos.integrations.google.gmail;

public record GoogleGmailMessage(
    String id,
    String threadId,
    String from,
    String subject,
    String date,
    String snippet
) {}
