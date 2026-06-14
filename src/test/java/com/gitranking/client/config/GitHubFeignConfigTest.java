package com.gitranking.client.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GitHubFeignConfigTest {

    @Test
    void whenTokenIsBlank_noAuthorizationHeaderIsAdded() {
        RequestInterceptor interceptor = new GitHubFeignConfig("").githubRequestInterceptor();

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertThat(template.headers()).doesNotContainKey("Authorization");
    }

    @Test
    void whenTokenIsSet_authorizationBearerHeaderIsAdded() {
        RequestInterceptor interceptor = new GitHubFeignConfig("my-secret-token").githubRequestInterceptor();

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertThat(template.headers()).containsKey("Authorization");
        assertThat(template.headers().get("Authorization"))
                .containsExactly("Bearer my-secret-token");
    }
}
