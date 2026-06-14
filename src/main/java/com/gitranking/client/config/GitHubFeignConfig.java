package com.gitranking.client.config;

import com.gitranking.client.GitHubApiErrorDecoder;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign configuration for the GitHub API client.
 *
 * <p>If {@code github.token} is set in application properties, it is attached as a
 * Bearer token on every request. If absent, requests are made unauthenticated
 * (subject to GitHub's lower rate limits).
 */
@Configuration
public class GitHubFeignConfig {

    private static final String GITHUB_ACCEPT_HEADER = "application/vnd.github+json";
    private static final String GITHUB_API_VERSION = "2022-11-28";

    @Value("${github.token:}")
    private String bearerToken;

    @Bean
    public GitHubApiErrorDecoder errorDecoder() {
        return new GitHubApiErrorDecoder();
    }

    @Bean
    public RequestInterceptor githubRequestInterceptor() {
        return template -> {
            if (bearerToken != null && !bearerToken.isBlank()) {
                template.header("Authorization", "Bearer " + bearerToken);
            }
            template.header("Accept", GITHUB_ACCEPT_HEADER);
            template.header("X-GitHub-Api-Version", GITHUB_API_VERSION);
        };
    }
}
