package com.gitranking.client;

import com.gitranking.exception.GitHubAuthException;
import com.gitranking.exception.GitHubRateLimitException;
import com.gitranking.exception.GitHubUpstreamException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class GitHubApiErrorDecoderTest {

    private GitHubApiErrorDecoder decoder;

    @BeforeEach
    void setUp() {
        decoder = new GitHubApiErrorDecoder();
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 403})
    void authStatuses_throwGitHubAuthException(int status) {
        Exception ex = decoder.decode("GitHubClient#search", response(status, "Forbidden"));

        assertThat(ex).isInstanceOf(GitHubAuthException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"You have exceeded a secondary rate limit", "API rate limit exceeded", "abuse detected"})
    void status403_withRateLimitBody_throwsGitHubRateLimitException(String body) {
        Exception ex = decoder.decode("GitHubClient#search", response(403, body));

        assertThat(ex).isInstanceOf(GitHubRateLimitException.class);
    }

    @Test
    void status429_throwsGitHubRateLimitException() {
        Exception ex = decoder.decode("GitHubClient#search", response(429));

        assertThat(ex).isInstanceOf(GitHubRateLimitException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {500, 502, 503, 504})
    void upstreamStatuses_throwGitHubUpstreamException(int status) {
        Exception ex = decoder.decode("GitHubClient#search", response(status));

        assertThat(ex).isInstanceOf(GitHubUpstreamException.class);
    }

    @Test
    void upstreamException_carriesHttpStatus() {
        Exception ex = decoder.decode("GitHubClient#search", response(503));

        assertThat(((GitHubUpstreamException) ex).getStatusCode()).isEqualTo(503);
    }

    @Test
    void authException_carriesHttpStatus() {
        Exception ex = decoder.decode("GitHubClient#search", response(403));

        assertThat(((GitHubAuthException) ex).getStatusCode()).isEqualTo(403);
    }

    @Test
    void unrecognisedStatus_delegatesToDefaultDecoder() {
        Exception ex = decoder.decode("GitHubClient#search", response(422));

        assertThat(ex)
                .isNotInstanceOf(GitHubAuthException.class)
                .isNotInstanceOf(GitHubRateLimitException.class)
                .isNotInstanceOf(GitHubUpstreamException.class);
    }

    // --- helpers ---

    private Response response(int status) {
        return response(status, "");
    }

    private Response response(int status, String body) {
        return Response.builder()
                .status(status)
                .reason("reason")
                .headers(Collections.emptyMap())
                .request(Request.create(
                        Request.HttpMethod.GET,
                        "https://api.github.com/search/repositories",
                        Collections.emptyMap(),
                        null,
                        StandardCharsets.UTF_8,
                        null))
                .body(body, StandardCharsets.UTF_8)
                .build();
    }
}
