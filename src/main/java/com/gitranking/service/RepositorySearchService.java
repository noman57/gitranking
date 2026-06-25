package com.gitranking.service;

import com.gitranking.client.GitHubClient;
import com.gitranking.client.model.GitHubRepository;
import com.gitranking.client.model.GitHubSearchResponse;
import com.gitranking.model.PagedResult;
import com.gitranking.model.ProgrammingLanguage;
import com.gitranking.model.RepositoryResult;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
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
     * @param language     programming language filter, may be null
     * @param createdAfter only include repositories created on or after this date, may be null
     * @param perPage      number of results per page (max 100)
     * @param page         page number (1-based)
     * @return paged result containing scored repositories and pagination metadata
     */
    @Cacheable(value = "repositories", key = "{#language, #createdAfter, #perPage, #page}")
    @Retry(name = "githubSearch")
    public PagedResult<RepositoryResult> search(ProgrammingLanguage language, LocalDate createdAfter, int perPage, int page) {
        String query = buildQuery(language, createdAfter);
        log.debug("Searching repositories — query: '{}', perPage: {}, page: {}", query, perPage, page);
        GitHubSearchResponse response = gitHubClient.searchRepositories(query, "updated", "desc", perPage, page);
        List<GitHubRepository> items = response.getItems() != null ? response.getItems() : Collections.emptyList();
        log.debug("GitHub returned {} repositories", items.size());
        if (response.isIncompleteResults()) {
            log.warn("GitHub returned incomplete results for query '{}' — results may be partial", query);
        }

        Instant now = Instant.now();
        log.debug("Scoring {} repositories", items.size());
        List<RepositoryResult> results = items.stream()
                .map(repo -> new RepositoryResult(repo.getFullName(), repo.getHtmlUrl(), scorer.score(repo, now)))
                .toList();
        return new PagedResult<>(page, perPage, response.getTotalCount(), results);
    }

    private String buildQuery(ProgrammingLanguage language, LocalDate createdAfter) {
        StringBuilder q = new StringBuilder("is:public");
        if (language != null) {
            q.append(" language:").append(language.getValue());
        }
        if (createdAfter != null) {
            q.append(" created:>=").append(createdAfter);
        }
        return q.toString();
    }
}
