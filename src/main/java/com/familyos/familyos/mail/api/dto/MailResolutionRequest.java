package com.familyos.familyos.mail.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record MailResolutionRequest(
        @NotBlank String optionId,
        @Email String delegateToEmail,
        @Email String notifyRecipientEmail
) {
}
