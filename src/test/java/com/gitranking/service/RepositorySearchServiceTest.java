package com.gitranking.service;

import com.gitranking.client.GitHubClient;
import com.gitranking.client.model.GitHubSearchResponse;
import com.gitranking.service.PopularityScorer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositorySearchServiceTest {

    @Mock
    private GitHubClient gitHubClient;

    @Mock
    private PopularityScorer scorer;

    @InjectMocks
    private RepositorySearchService service;

    @Test
    void search_buildsQueryWithLanguageAndCreatedAfter() {
        GitHubSearchResponse response = new GitHubSearchResponse();
        response.setItems(List.of());
        response.setTotalCount(0);
        response.setIncompleteResults(false);

        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);

        when(gitHubClient.searchRepositories(queryCaptor.capture(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(response);

        service.search("java", LocalDate.of(2024, 1, 1), 10, 1);

        String capturedQuery = queryCaptor.getValue();
        assertThat(capturedQuery).contains("language:java");
        assertThat(capturedQuery).contains("created:>=2024-01-01");
    }

    @Test
    void search_buildsQueryWithoutOptionalParams_whenNull() {
        GitHubSearchResponse response = new GitHubSearchResponse();
        response.setItems(List.of());
        response.setTotalCount(0);
        response.setIncompleteResults(false);

        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);

        when(gitHubClient.searchRepositories(queryCaptor.capture(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(response);

        service.search(null, null, 10, 1);

        String capturedQuery = queryCaptor.getValue();
        assertThat(capturedQuery).isEqualTo("is:public");
    }

    @Test
    void search_ignoresBlankLanguage() {
        GitHubSearchResponse response = new GitHubSearchResponse();
        response.setItems(List.of());
        response.setTotalCount(0);
        response.setIncompleteResults(false);

        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);

        when(gitHubClient.searchRepositories(queryCaptor.capture(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(response);

        service.search(" ", null, 10, 1);

        String capturedQuery = queryCaptor.getValue();
        assertThat(capturedQuery).isEqualTo("is:public");
        assertThat(capturedQuery).doesNotContain("language:");
    }

    @Test
    void search_buildsQueryWithCreatedAfterOnly() {
        GitHubSearchResponse response = new GitHubSearchResponse();
        response.setItems(List.of());
        response.setTotalCount(0);
        response.setIncompleteResults(false);

        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);

        when(gitHubClient.searchRepositories(queryCaptor.capture(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(response);

        service.search(null, LocalDate.of(2024, 1, 1), 10, 1);

        String capturedQuery = queryCaptor.getValue();
        assertThat(capturedQuery).contains("created:>=2024-01-01");
        assertThat(capturedQuery).doesNotContain("language:");
    }
}
