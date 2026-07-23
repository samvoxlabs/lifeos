package com.familyos.familyos.mail.model;

import java.time.OffsetDateTime;

public record ExtractedMailEventCandidate(
        String title,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        String location,
        String description,
        String type,
        String category,
        String priority,
        double confidence
) {
}
