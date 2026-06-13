package com.gitranking.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class GitHubSearchResponse {

    @JsonProperty("total_count")
    private Integer totalCount;

    @JsonProperty("incomplete_results")
    private Boolean incompleteResults;

    private List<GitHubRepository> items;
}
