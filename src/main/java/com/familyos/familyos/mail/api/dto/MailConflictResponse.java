package com.familyos.familyos.mail.api.dto;

import java.util.List;

public record MailConflictResponse(
        String id,
        String type,
        String title,
        String description,
        List<String> eventIds,
        String status,
        List<MailConflictOptionResponse> options
) {
}
