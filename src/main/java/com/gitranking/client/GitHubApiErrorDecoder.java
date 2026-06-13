package com.gitranking.client;

import com.gitranking.exception.GitHubAuthException;
import com.gitranking.exception.GitHubRateLimitException;
import com.gitranking.exception.GitHubUpstreamException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;


@Slf4j
public class GitHubApiErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        log.debug("Decoding GitHub error response — method: {}, status: {}", methodKey, response.status());
        String body = readBody(response);
        return switch (response.status()) {
            case 401, 403 -> new GitHubAuthException(
                    "GitHub auth failure [%d] on %s: %s".formatted(response.status(), methodKey, body),
                    response.status());
            case 429 -> new GitHubRateLimitException(
                    "GitHub rate limit hit on %s: %s".formatted(methodKey, body));
            case 500, 502, 503, 504 -> new GitHubUpstreamException(
                    "GitHub upstream error [%d] on %s: %s".formatted(response.status(), methodKey, body),
                    response.status());
            default -> defaultDecoder.decode(methodKey, response);
        };
    }

    private String readBody(Response response) {
        if (response.body() == null) return "(no body)";
        try (InputStream is = response.body().asInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "(unreadable body)";
        }
    }
}
