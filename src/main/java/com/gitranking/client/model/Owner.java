package com.gitranking.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Owner(
        String login,
        @JsonProperty("avatar_url") String avatarUrl
) {}
