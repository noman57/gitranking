package com.gitranking.dto;

import lombok.Value;

import java.util.List;

/**
 * Generic paginated response wrapper.
 *
 * @param <T> type of items in the result set
 */
@Value
public class PagedResult<T> {

    /** Current page number (1-based). */
    int page;

    /** Number of items requested per page. */
    int perPage;

    /** Total number of matching results on GitHub (may be capped at 1000 by GitHub). */
    int totalCount;

    /** Whether GitHub's result set is incomplete due to query timeout. */
    boolean incompleteResults;

    /** Scored repositories for this page, in GitHub result order. */
    List<T> items;
}
