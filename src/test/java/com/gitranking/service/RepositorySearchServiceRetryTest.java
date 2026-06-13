package com.gitranking.service;

import com.gitranking.client.GitHubClient;
import com.gitranking.exception.GitHubUpstreamException;
import com.gitranking.service.PopularityScorer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositorySearchServiceRetryTest {

    @Mock
    private GitHubClient gitHubClient;

    @Mock
    private PopularityScorer scorer;

    @InjectMocks
    private RepositorySearchService repositorySearchService;

    @Test
    void search_propagatesUpstreamException_whenGitHubClientThrows() {
        when(gitHubClient.searchRepositories(any(), any(), any(), anyInt(), anyInt()))
            .thenThrow(new GitHubUpstreamException("GitHub 503 - service unavailable", 503));

        assertThatThrownBy(() -> repositorySearchService.search("java", LocalDate.now(), 10, 1))
                .isInstanceOf(GitHubUpstreamException.class)
                .hasMessageContaining("GitHub 503 - service unavailable");
    }
}
