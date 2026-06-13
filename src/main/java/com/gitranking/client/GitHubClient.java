package com.gitranking.client;

import com.gitranking.client.config.GitHubFeignConfig;
import com.gitranking.client.dto.GitHubSearchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "github", url = "${github.api.url}", configuration = GitHubFeignConfig.class)
public interface GitHubClient {

    @GetMapping("/search/repositories")
    GitHubSearchResponse searchRepositories(
            @RequestParam("q") String q,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "order", required = false) String order,
            @RequestParam(value = "per_page", required = false, defaultValue = "30") int perPage,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page
    );
}
