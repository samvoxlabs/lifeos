package com.familyos.familyos.integrations.google.people;

import com.familyos.familyos.config.properties.GoogleProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GooglePeopleClientImplTest {

    @Test
    void fetchContactsUsesConnectionsCollectionQuery() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        GoogleProperties properties = new GoogleProperties(
                new GoogleProperties.Apis(
                        "https://gmail.googleapis.com/gmail/v1",
                        "https://www.googleapis.com/calendar/v3",
                        "https://www.googleapis.com/drive/v3",
                        "https://tasks.googleapis.com/tasks/v1",
                        "https://people.googleapis.com/v1"
                ),
                new GoogleProperties.Gmail("me", 10),
                new GoogleProperties.Calendar("primary", 10),
                new GoogleProperties.Drive(10),
                new GoogleProperties.Tasks(10),
                new GoogleProperties.People(20)
        );

        server.expect(requestTo("https://people.googleapis.com/v1/people/me/connections?pageSize=20&sources=READ_SOURCE_TYPE_CONTACT&sources=READ_SOURCE_TYPE_PROFILE&personFields=names,emailAddresses,phoneNumbers"))
                .andExpect(header("Authorization", "Bearer access-token"))
                .andRespond(withSuccess("""
                        {"connections":[{"resourceName":"people/c123","names":[{"displayName":"Alex Doe"}],"emailAddresses":[{"value":"alex@example.com"}],"phoneNumbers":[{"value":"+1-555-0100"}]}]}
                        """, MediaType.APPLICATION_JSON));

        GooglePeopleClientImpl client = new GooglePeopleClientImpl(properties, builder);

        var contacts = client.fetchContacts("access-token", 20);

        assertEquals(1, contacts.size());
        assertEquals("people/c123", contacts.get(0).resourceName());
        assertEquals("Alex Doe", contacts.get(0).displayName());
        server.verify();
    }
}
