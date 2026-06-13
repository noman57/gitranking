package com.gitranking.dto;

import lombok.Getter;

import java.time.Instant;

/**
 * Uniform error response body. Message is always safe for client consumption.
 */
@Getter
public class ErrorResponse {

    private final int status;
    private final String message;
    private final Instant timestamp;

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = Instant.now();
    }
}
