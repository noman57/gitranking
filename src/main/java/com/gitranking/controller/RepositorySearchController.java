package com.gitranking.controller;

import com.gitranking.model.PagedResult;
import com.gitranking.model.RepositoryResult;
import com.gitranking.service.RepositorySearchService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Validated
@RestController
@RequestMapping("/v1/repositories")
public class RepositorySearchController {

    private final RepositorySearchService searchService;

    public RepositorySearchController(RepositorySearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<PagedResult<RepositoryResult>> search(
            @RequestParam(required = false) String language,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdAfter,
            @RequestParam(defaultValue = "30") @Min(1) @Max(100) int perPage,
            @RequestParam(defaultValue = "1") @Min(1) int page
    ) {
        return ResponseEntity.ok(searchService.search(language, createdAfter, perPage, page));
    }
}
