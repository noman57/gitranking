package com.gitranking.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Owner {

    private String login;

    @JsonProperty("avatar_url")
    private String avatarUrl;
}
