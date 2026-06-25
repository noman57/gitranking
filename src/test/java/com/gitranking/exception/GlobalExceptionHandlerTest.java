package com.gitranking.exception;

import com.gitranking.controller.RepositorySearchController;
import com.gitranking.service.RepositorySearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RepositorySearchController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RepositorySearchService repositorySearchService;

    @Test
    void whenRateLimitExceeded_returns502WithSafeMessage() throws Exception {
        when(repositorySearchService.search(any(), any(), anyInt(), anyInt()))
                .thenThrow(new GitHubRateLimitException("raw github detail"));

        mockMvc.perform(get("/v1/repositories"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("raw github detail"))));
    }

    @Test
    void whenGitHubIsUnavailable_returns502WithSafeMessage() throws Exception {
        when(repositorySearchService.search(any(), any(), anyInt(), anyInt()))
                .thenThrow(new GitHubUpstreamException("internal detail", 503));

        mockMvc.perform(get("/v1/repositories"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("internal detail"))));
    }

    @Test
    void whenCreatedAfterParamIsInvalid_returns400() throws Exception {
        mockMvc.perform(get("/v1/repositories").param("createdAfter", "not-a-date"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void whenUnexpectedErrorOccurs_returns500WithSafeMessage() throws Exception {
        when(repositorySearchService.search(any(), any(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("sensitive internal detail"));

        mockMvc.perform(get("/v1/repositories"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("sensitive internal detail"))));
    }

    @Test
    void whenAuthFails_returns502WithSafeMessage() throws Exception {
        when(repositorySearchService.search(any(), any(), anyInt(), anyInt()))
                .thenThrow(new GitHubAuthException("internal auth detail", 401));

        mockMvc.perform(get("/v1/repositories"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("internal auth detail"))));
    }

    @Test
    void whenPerPageExceedsMax_returns400() throws Exception {
        mockMvc.perform(get("/v1/repositories").param("perPage", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
