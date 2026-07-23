package com.familyos.familyos.mail.api.dto;

import jakarta.validation.constraints.NotNull;

public record MailMessagePatchRequest(
        @NotNull Boolean read
) {
}
