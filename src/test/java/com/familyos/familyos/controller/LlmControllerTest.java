package com.familyos.familyos.controller;

import com.familyos.familyos.config.TestSecurityConfig;
import com.familyos.familyos.llm.LlmResponse;
import com.familyos.familyos.service.LlmService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
class LlmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

    @MockBean
    private LlmService llmService;

    @Test
    @WithMockUser
    void testEndpointWorksOnLegacyPath() throws Exception {
        when(llmService.test("Say hello")).thenReturn(new LlmResponse(
                "gemini",
                "gemini-2.0-flash",
                "hello",
                Map.of("mode", "test")
        ));

        mockMvc.perform(get("/llm/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("gemini"))
                .andExpect(jsonPath("$.content").value("hello"));
    }

    @Test
    @WithMockUser
    void testEndpointWorksOnApiPath() throws Exception {
        when(llmService.test("Say hello")).thenReturn(new LlmResponse(
                "gemini",
                "gemini-2.0-flash",
                "hello",
                Map.of("mode", "test")
        ));

        mockMvc.perform(get("/api/llm/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("gemini"))
                .andExpect(jsonPath("$.content").value("hello"));
    }
}
