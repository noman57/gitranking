package com.gitranking.exception;

/** Thrown when the GitHub API responds with a 5xx server error. */
public class GitHubUpstreamException extends GitHubApiException {

    public GitHubUpstreamException(String internalDetail, int statusCode) {
        super(internalDetail, statusCode);
    }
}
