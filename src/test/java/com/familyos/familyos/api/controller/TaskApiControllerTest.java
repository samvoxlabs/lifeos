package com.familyos.familyos.api.controller;

import com.familyos.familyos.api.dto.PagedResponse;
import com.familyos.familyos.api.dto.TaskResponse;
import com.familyos.familyos.api.service.TaskApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskApiController.class)
class TaskApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskApiService taskApiService;

    @MockBean private OAuth2AuthorizedClientService authorizedClientService;
    @MockBean private ClientRegistrationRepository clientRegistrationRepository;

    @Test
    @WithMockUser
    void returnsPagedTasks() throws Exception {
        TaskResponse task = new TaskResponse(
            UUID.randomUUID(),
            "Confirm attendance",
            "Reply to teacher",
            "OPEN",
            "HIGH",
            0.9,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        when(taskApiService.getTasks(any(), any(), any()))
            .thenReturn(new PagedResponse<>(List.of(task), 0, 20, 1, 1, "createdAt: DESC"));

        mockMvc.perform(get("/api/tasks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].title").value("Confirm attendance"));
    }
}
