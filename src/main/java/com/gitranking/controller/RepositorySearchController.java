package com.gitranking.controller;

import com.gitranking.dto.PagedResult;
import com.gitranking.dto.RepositoryResult;
import com.gitranking.service.RepositorySearchService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Exposes repository search and ranking endpoints.
 */
@RestController
@RequestMapping("/repositories")
public class RepositorySearchController {

    private final RepositorySearchService searchService;

    public RepositorySearchController(RepositorySearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * Search GitHub repositories ranked by popularity.
     *
     * @param language     filter by programming language (optional)
     * @param createdAfter only include repositories created on or after this date (optional, ISO format: yyyy-MM-dd)
     * @param perPage      results per page, default 30, max 100
     * @param page         page number, default 1
     * @return scored repositories in GitHub result order, each enriched with a popularity score
     */
    @GetMapping
    public ResponseEntity<PagedResult<RepositoryResult>> search(
            @RequestParam(required = false) String language,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdAfter,
            @RequestParam(defaultValue = "30") int perPage,
            @RequestParam(defaultValue = "1") int page
    ) {
        return ResponseEntity.ok(searchService.search(language, createdAfter, perPage, page));
    }
}
