package com.familyos.familyos.mail.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record MailSyncRequest(
        String mailbox,
        String cursor,
        @Min(1) @Max(100) Integer maxResults
) {
}
