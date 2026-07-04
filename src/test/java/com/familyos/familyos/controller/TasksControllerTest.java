package com.familyos.familyos.controller;

import com.familyos.familyos.authentication.service.AuthenticationService;
import com.familyos.familyos.config.TestSecurityConfig;
import com.familyos.familyos.dto.AuthenticatedUser;
import com.familyos.familyos.dto.TaskItemDto;
import com.familyos.familyos.service.TasksService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
class TasksControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

    @MockBean
    private TasksService tasksService;

    @MockBean
    private AuthenticationService authenticationService;

    @Test
    @WithMockUser
    void tasksReturnsTaskItems() throws Exception {
        when(authenticationService.currentUser()).thenReturn(new AuthenticatedUser("user-id", "user@example.com", "Test User", "google"));
        when(tasksService.readTasks("user-id")).thenReturn(List.of(
                new TaskItemDto("task-1", "Pay bills", "Due this week", "needsAction", "2026-07-10T00:00:00.000Z", "2026-07-03T20:00:00.000Z")
        ));

        mockMvc.perform(get("/google-tasks/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Pay bills"));
    }
}
