package com.gitranking.exception;

import java.time.Instant;

/**
 * Uniform error response body. Message is always safe for client consumption.
 */
public record ErrorResponse(int status, String message, Instant timestamp) {

    public static ErrorResponse of(int status, String message) {
        return new ErrorResponse(status, message, Instant.now());
    }
}
