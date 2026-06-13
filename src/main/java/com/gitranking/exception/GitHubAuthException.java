package com.gitranking.exception;

/** Thrown when the GitHub API responds with 401 Unauthorized or 403 Forbidden. */
public class GitHubAuthException extends GitHubApiException {

    public GitHubAuthException(String internalDetail, int statusCode) {
        super(internalDetail, statusCode);
    }
}
