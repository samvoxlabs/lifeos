package com.familyos.familyos.extraction.exception;

public class InvalidResponseException extends ExtractionException {
    public InvalidResponseException(String message) {
        super(message);
    }

    public InvalidResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
