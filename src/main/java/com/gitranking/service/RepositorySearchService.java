package com.gitranking.service;

import com.gitranking.client.GitHubClient;
import com.gitranking.dto.GitHubSearchResponse;
import com.gitranking.dto.PagedResult;
import com.gitranking.dto.RepositoryResult;
import com.gitranking.scoring.PopularityScorer;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Fetches repositories from GitHub and enriches them with a popularity score.
 */
@Slf4j
@Service
public class RepositorySearchService {

    private final GitHubClient gitHubClient;
    private final PopularityScorer scorer;

    public RepositorySearchService(GitHubClient gitHubClient, PopularityScorer scorer) {
        this.gitHubClient = gitHubClient;
        this.scorer = scorer;
    }

    /**
     * Searches GitHub for repositories matching the given criteria and returns them
     * in the order GitHub provided them.
     *
     * @param language     programming language filter (e.g. "java"), may be null
     * @param createdAfter only include repositories created on or after this date, may be null
     * @param perPage      number of results per page (max 100)
     * @param page         page number (1-based)
     * @return paged result containing scored repositories and pagination metadata
     */
    @Retry(name = "githubSearch")
    public PagedResult<RepositoryResult> search(String language, LocalDate createdAfter, int perPage, int page) {
        String query = buildQuery(language, createdAfter);
        log.debug("Searching repositories — query: '{}', perPage: {}, page: {}", query, perPage, page);
        GitHubSearchResponse response = gitHubClient.searchRepositories(query, "updated", "desc", perPage, page);
        log.debug("GitHub returned {} repositories (incomplete: {})", response.getItems().size(), response.getIncompleteResults());

        Instant now = Instant.now();
        log.debug("Scoring {} repositories", response.getItems().size());
        List<RepositoryResult> results = response.getItems().stream()
                .map(repo -> new RepositoryResult(repo.getFullName(), repo.getHtmlUrl(), scorer.score(repo, now)))
                .toList();
        return new PagedResult<>(page, perPage, response.getTotalCount(), response.getIncompleteResults(), results);
    }

    private String buildQuery(String language, LocalDate createdAfter) {
        StringBuilder q = new StringBuilder("is:public");
        if (language != null && !language.isBlank()) {
            q.append(" language:").append(language);
        }
        if (createdAfter != null) {
            q.append(" created:>=").append(createdAfter);
        }
        return q.toString();
    }
}
