package com.gitranking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OwnerDto {

    private String login;

    @JsonProperty("avatar_url")
    private String avatarUrl;
}
