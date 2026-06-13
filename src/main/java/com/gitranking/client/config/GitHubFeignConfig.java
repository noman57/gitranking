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
            template.header("Accept", "application/vnd.github+json");
            template.header("X-GitHub-Api-Version", "2022-11-28");
        };
    }
}
