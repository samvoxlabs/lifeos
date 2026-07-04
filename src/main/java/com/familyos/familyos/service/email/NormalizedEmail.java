package com.familyos.familyos.service.email;

public record NormalizedEmail(
        String id,
        String threadId,
        String from,
        String subject,
        String date,
        String snippet
) {}
