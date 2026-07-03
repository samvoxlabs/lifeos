package com.familyos.familyos.controller;

import com.familyos.familyos.config.TestSecurityConfig;
import com.familyos.familyos.dto.AuthenticatedUser;
import com.familyos.familyos.dto.CalendarEventDto;
import com.familyos.familyos.authentication.service.AuthenticationService;
import com.familyos.familyos.service.CalendarService;
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
class CalendarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

    @MockBean
    private CalendarService calendarService;

    @MockBean
    private AuthenticationService authenticationService;

    @Test
    @WithMockUser
    void eventsReturnsCalendarEvents() throws Exception {
        when(authenticationService.currentUser()).thenReturn(new AuthenticatedUser("user-id", "user@example.com", "Test User", "google"));
        when(calendarService.readUpcomingEvents("user-id")).thenReturn(List.of(
                new CalendarEventDto("1", "Standup", "Zoom", "Daily standup", "confirmed", "2026-07-03T09:00:00-05:00", "2026-07-03T09:15:00-05:00")
        ));

        mockMvc.perform(get("/calendar/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].summary").value("Standup"));
    }
}
