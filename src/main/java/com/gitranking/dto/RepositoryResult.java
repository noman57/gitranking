package com.gitranking.dto;

import lombok.Value;

/**
 * Slim API response for a ranked repository — name, link, and popularity score only.
 */
@Value
public class RepositoryResult {

    String name;
    String url;
    double popularityScore;
}
