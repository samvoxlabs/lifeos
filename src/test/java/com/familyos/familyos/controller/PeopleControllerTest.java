package com.familyos.familyos.controller;

import com.familyos.familyos.authentication.service.AuthenticationService;
import com.familyos.familyos.config.TestSecurityConfig;
import com.familyos.familyos.dto.AuthenticatedUser;
import com.familyos.familyos.dto.ContactDto;
import com.familyos.familyos.service.PeopleService;
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
class PeopleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

    @MockBean
    private PeopleService peopleService;

    @MockBean
    private AuthenticationService authenticationService;

    @Test
    @WithMockUser
    void contactsReturnsContacts() throws Exception {
        when(authenticationService.currentUser()).thenReturn(new AuthenticatedUser("user-id", "user@example.com", "Test User", "google"));
        when(peopleService.readContacts("user-id")).thenReturn(List.of(
                new ContactDto("people/c123", "Alex Doe", "alex@example.com", "+1-555-0100")
        ));

        mockMvc.perform(get("/people/contacts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].displayName").value("Alex Doe"));
    }
}
