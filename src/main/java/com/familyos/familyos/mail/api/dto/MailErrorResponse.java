package com.familyos.familyos.mail.api.dto;

public record MailErrorResponse(
        ErrorBody error
) {
    public record ErrorBody(
            String code,
            String message,
            boolean retryable,
            String requestId
    ) {
    }
}
