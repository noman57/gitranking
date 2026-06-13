package com.gitranking.client.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class GitHubFeignConfigTest {

    private GitHubFeignConfig configWithToken(String token) throws Exception {
        GitHubFeignConfig config = new GitHubFeignConfig();
        Field field = GitHubFeignConfig.class.getDeclaredField("bearerToken");
        field.setAccessible(true);
        field.set(config, token);
        return config;
    }

    @Test
    void whenTokenIsBlank_noAuthorizationHeaderIsAdded() throws Exception {
        GitHubFeignConfig config = configWithToken("");
        RequestInterceptor interceptor = config.githubRequestInterceptor();

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertThat(template.headers()).doesNotContainKey("Authorization");
    }

    @Test
    void whenTokenIsNull_noAuthorizationHeaderIsAdded() throws Exception {
        GitHubFeignConfig config = configWithToken(null);
        RequestInterceptor interceptor = config.githubRequestInterceptor();

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertThat(template.headers()).doesNotContainKey("Authorization");
    }

    @Test
    void whenTokenIsSet_authorizationBearerHeaderIsAdded() throws Exception {
        GitHubFeignConfig config = configWithToken("my-secret-token");
        RequestInterceptor interceptor = config.githubRequestInterceptor();

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertThat(template.headers()).containsKey("Authorization");
        assertThat(template.headers().get("Authorization"))
                .containsExactly("Bearer my-secret-token");
    }
}
