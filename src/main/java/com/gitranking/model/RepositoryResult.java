package com.gitranking.model;

/**
 * Slim API response for a ranked repository — name, link, and popularity score only.
 */
public record RepositoryResult(String name, String url, double popularityScore) {}
