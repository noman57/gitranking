package com.gitranking.exception;

/** Thrown when the GitHub API responds with 429 Too Many Requests. */
public class GitHubRateLimitException extends GitHubApiException {

    public GitHubRateLimitException(String internalDetail) {
        super(internalDetail, 429);
    }
}
