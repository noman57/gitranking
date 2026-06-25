package com.gitranking.service;

import com.gitranking.client.GitHubClient;
import com.gitranking.exception.GitHubUpstreamException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
    "resilience4j.retry.instances.githubSearch.wait-duration=0ms",
    "spring.cache.type=none"
})
class RepositorySearchServiceRetryTest {

    @MockBean
    private GitHubClient gitHubClient;

    @Autowired
    private RepositorySearchService repositorySearchService;

    @Test
    void search_retriesUpToMaxAttempts_thenPropagatesException() {
        when(gitHubClient.searchRepositories(any(), any(), any(), anyInt(), anyInt()))
            .thenThrow(new GitHubUpstreamException("GitHub 503 - service unavailable", 503));

        assertThatThrownBy(() -> repositorySearchService.search("java", LocalDate.now(), 10, 1))
                .isInstanceOf(GitHubUpstreamException.class)
                .hasMessageContaining("GitHub 503 - service unavailable");

        verify(gitHubClient, times(3)).searchRepositories(any(), any(), any(), anyInt(), anyInt());
    }
}
