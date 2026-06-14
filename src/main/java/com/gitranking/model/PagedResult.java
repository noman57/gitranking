package com.gitranking.model;

import java.util.List;

/**
 * Generic paginated response wrapper.
 *
 * @param <T> type of items in the result set
 */
public record PagedResult<T>(
        int page,
        int perPage,
        int totalCount,
        List<T> items
) {}
