package com.gitranking.cache;

import com.gitranking.service.RepositorySearchService;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import com.gitranking.client.GitHubClient;
import com.gitranking.client.model.GitHubSearchResponse;

import java.util.List;

@SpringBootTest
@Testcontainers
class RepositorySearchCacheIT {

    @Container
    static RedisContainer redis = new RedisContainer(RedisContainer.DEFAULT_IMAGE_NAME.withTag("latest"));

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @MockBean
    private GitHubClient gitHubClient;

    @Autowired
    private RepositorySearchService searchService;

    @Test
    void secondIdenticalRequest_doesNotHitGitHubClient() {
        GitHubSearchResponse response = new GitHubSearchResponse();
        response.setTotalCount(1);
        response.setIncompleteResults(false);
        response.setItems(List.of());
        when(gitHubClient.searchRepositories(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(response);

        searchService.search("java", null, 10, 1);
        searchService.search("java", null, 10, 1);

        verify(gitHubClient, times(1)).searchRepositories(any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    void requestsWithDifferentParams_areCachedSeparately() {
        GitHubSearchResponse response = new GitHubSearchResponse();
        response.setTotalCount(1);
        response.setIncompleteResults(false);
        response.setItems(List.of());
        when(gitHubClient.searchRepositories(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(response);

        searchService.search("java", null, 10, 1);
        searchService.search("python", null, 10, 1);

        verify(gitHubClient, times(2)).searchRepositories(any(), any(), any(), anyInt(), anyInt());
    }
}
