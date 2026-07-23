package com.familyos.familyos.mail.api.dto;

import java.util.Map;

public record MailConflictOptionResponse(
        String id,
        String label,
        String description,
        boolean recommended,
        Map<String, Object> effects
) {
}
