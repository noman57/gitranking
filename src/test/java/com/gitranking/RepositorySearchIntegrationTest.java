package com.gitranking;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "spring.cache.type=none")
class RepositorySearchIntegrationTest {

    private static final int WIREMOCK_PORT = 8089;

    private WireMockServer wireMockServer;

    @LocalServerPort
    int port;

    @DynamicPropertySource
    static void overrideGitHubApiUrl(DynamicPropertyRegistry registry) {
        registry.add("github.api.url", () -> "http://localhost:" + WIREMOCK_PORT);
    }

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(wireMockConfig().port(WIREMOCK_PORT).usingFilesUnderClasspath("."));
        wireMockServer.start();
        RestAssured.port = port;

        wireMockServer.stubFor(get(urlPathMatching("/search/repositories"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("github-search-two-repos.json")));
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void repositoriesAreEnrichedWithPopularityScore() {
        List<Float> scores = given()
                .queryParam("language", "java")
                .when()
                .get("/repositories")
                .then()
                .statusCode(200)
                .body("items", hasSize(2))
                .body("page", equalTo(1))
                .body("totalCount", greaterThan(0))
                .extract()
                .jsonPath()
                .getList("items.popularityScore", Float.class);

        assertThat(scores).hasSize(2);
        assertThat(scores).allSatisfy(score -> assertThat(score).isGreaterThan(0));
    }
}
