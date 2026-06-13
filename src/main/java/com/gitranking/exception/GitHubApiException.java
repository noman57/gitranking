package com.gitranking.exception;

import lombok.Getter;

/**
 * Base exception for all errors originating from the GitHub API.
 * Carries an internal detail message for logging that must never be surfaced to clients.
 */
@Getter
public class GitHubApiException extends RuntimeException {

    private final int statusCode;

    public GitHubApiException(String internalDetail, int statusCode) {
        super(internalDetail);
        this.statusCode = statusCode;
    }
}
