package com.gitranking.client;

import com.gitranking.exception.GitHubAuthException;
import com.gitranking.exception.GitHubRateLimitException;
import com.gitranking.exception.GitHubUpstreamException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;


@Slf4j
public class GitHubApiErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        String body = readBody(response);
        log.debug("GitHub error response — method: {}, status: {}, body: {}", methodKey, response.status(), body);
        return switch (response.status()) {
            case 401 -> new GitHubAuthException(
                    "GitHub auth failure [401] on %s".formatted(methodKey), 401);
            case 403 -> isRateLimitBody(body)
                    ? new GitHubRateLimitException("GitHub secondary rate limit on %s".formatted(methodKey))
                    : new GitHubAuthException("GitHub auth failure [403] on %s".formatted(methodKey), 403);
            case 429 -> new GitHubRateLimitException(
                    "GitHub rate limit hit on %s".formatted(methodKey));
            case 500, 502, 503, 504 -> new GitHubUpstreamException(
                    "GitHub upstream error [%d] on %s".formatted(response.status(), methodKey),
                    response.status());
            default -> defaultDecoder.decode(methodKey, response);
        };
    }

    private boolean isRateLimitBody(String body) {
        String lower = body.toLowerCase();
        return lower.contains("rate limit") || lower.contains("secondary rate") || lower.contains("abuse");
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
