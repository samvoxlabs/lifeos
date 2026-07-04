package com.familyos.familyos.extraction.controller;

import com.familyos.familyos.extraction.service.ExtractionService;
import com.familyos.familyos.ruleengine.dto.NormalizedDocument;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import com.familyos.familyos.extraction.dto.ExtractionResponse;

@SpringBootTest
@AutoConfigureMockMvc
class ExtractionControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private ExtractionService extractionService;

    @MockBean
    private OAuth2AuthorizedClientService authorizedClientService;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;
    
    @Test
    @WithMockUser
    void testProcessEndpointSuccess() throws Exception {
        NormalizedDocument document = new NormalizedDocument(
            "doc-1",
            "John Doe",
            "Meeting Tomorrow",
            "We have a meeting tomorrow at 2 PM.",
            List.of("work"),
            "1",
            "email"
        );
        
        ExtractionResponse mockResponse = ExtractionResponse.success(
            new com.familyos.familyos.extraction.dto.ExtractionResult(
                "Meeting scheduled",
                0.95,
                List.of()
            )
        );
        
        when(extractionService.process(any(NormalizedDocument.class)))
            .thenReturn(mockResponse);
        
        mockMvc.perform(post("/api/extraction/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(document)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.result.summary").value("Meeting scheduled"))
            .andExpect(jsonPath("$.result.confidence").value(0.95));
    }
    
    @Test
    @WithMockUser
    void testProcessEndpointSkipped() throws Exception {
        NormalizedDocument document = new NormalizedDocument(
            "doc-2",
            "Spam Sender",
            "Promotional",
            "Buy now!",
            List.of("spam"),
            "0",
            "email"
        );
        
        ExtractionResponse mockResponse = ExtractionResponse.skipped(
            new com.familyos.familyos.ruleengine.dto.RuleResult(
                com.familyos.familyos.ruleengine.dto.RuleDecision.IGNORE,
                "default",
                "Marked as spam",
                0
            )
        );
        
        when(extractionService.process(any(NormalizedDocument.class)))
            .thenReturn(mockResponse);
        
        mockMvc.perform(post("/api/extraction/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(document)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SKIPPED"));
    }
    
    @Test
    @WithMockUser
    void testProcessEndpointError() throws Exception {
        NormalizedDocument document = new NormalizedDocument(
            "doc-3",
            "Test",
            "Test",
            "Test",
            List.of(),
            "1",
            "email"
        );
        
        ExtractionResponse mockResponse = ExtractionResponse.error("LLM service unavailable");
        
        when(extractionService.process(any(NormalizedDocument.class)))
            .thenReturn(mockResponse);
        
        mockMvc.perform(post("/api/extraction/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(document)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ERROR"))
            .andExpect(jsonPath("$.message").value("LLM service unavailable"));
    }
    
    @Test
    void testProcessEndpointUnauthorized() throws Exception {
        NormalizedDocument document = new NormalizedDocument(
            "doc-1",
            "John Doe",
            "Meeting",
            "Meeting tomorrow",
            List.of(),
            "1",
            "email"
        );
        
        mockMvc.perform(post("/api/extraction/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(document)))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }
}
