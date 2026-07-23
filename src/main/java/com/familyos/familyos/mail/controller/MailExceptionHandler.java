package com.familyos.familyos.mail.controller;

import com.familyos.familyos.mail.api.dto.MailErrorResponse;
import com.familyos.familyos.mail.exception.MailApiException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.client.HttpClientErrorException;

import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice(basePackageClasses = MailController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MailExceptionHandler {

    @ExceptionHandler(MailApiException.class)
    public ResponseEntity<MailErrorResponse> handleMailApiException(MailApiException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(error(ex.getCode(), ex.getMessage(), ex.isRetryable()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MailErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest()
                .body(error("MAIL_SYNC_FAILED", message, false));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<MailErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(error("MAIL_SYNC_FAILED", "Invalid or missing request body", false));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<MailErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest()
                .body(error("MAIL_SYNC_FAILED", ex.getName() + " has an invalid format", false));
    }

    @ExceptionHandler(HttpClientErrorException.TooManyRequests.class)
    public ResponseEntity<MailErrorResponse> handleRateLimit(HttpClientErrorException.TooManyRequests ex) {
        return ResponseEntity.status(429)
                .body(error("GMAIL_RATE_LIMITED", "Gmail rate limit exceeded", true));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MailErrorResponse> handleUnexpected(Exception ex) {
        return ResponseEntity.internalServerError()
                .body(error("MAIL_SYNC_FAILED", ex.getMessage(), true));
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + " " + (fieldError.getDefaultMessage() == null ? "is invalid" : fieldError.getDefaultMessage());
    }

    private MailErrorResponse error(String code, String message, boolean retryable) {
        return new MailErrorResponse(new MailErrorResponse.ErrorBody(code, message, retryable, "request-" + UUID.randomUUID()));
    }
}
