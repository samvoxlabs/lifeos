package com.familyos.familyos.mail.model;

public record ResolutionOption(
        String key,
        String label,
        String description,
        boolean requiresDelegate,
        boolean requiresNotification
) {
}
