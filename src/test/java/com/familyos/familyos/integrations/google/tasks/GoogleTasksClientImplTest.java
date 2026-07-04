package com.familyos.familyos.integrations.google.tasks;

import com.familyos.familyos.config.properties.GoogleProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class GoogleTasksClientImplTest {

    @Test
    void fetchTasksResolvesTaskListBeforeFetchingTasks() {
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

        server.expect(requestTo("https://tasks.googleapis.com/tasks/v1/users/@me/lists"))
                .andExpect(header("Authorization", "Bearer access-token"))
                .andRespond(withSuccess("""
                        {"items":[{"id":"tasklist-1","title":"My Tasks"}]}
                        """, MediaType.APPLICATION_JSON));

        server.expect(requestTo("https://tasks.googleapis.com/tasks/v1/users/@me/lists/tasklist-1/tasks?maxResults=10&showCompleted=true&showHidden=false"))
                .andExpect(header("Authorization", "Bearer access-token"))
                .andRespond(withSuccess("""
                        {"items":[{"id":"task-1","title":"Pay bills","notes":"Due this week","status":"needsAction","due":"2026-07-10T00:00:00.000Z","updated":"2026-07-03T20:00:00.000Z"}]}
                        """, MediaType.APPLICATION_JSON));

        GoogleTasksClientImpl client = new GoogleTasksClientImpl(properties, builder);

        var tasks = client.fetchTasks("access-token", 10);

        assertEquals(1, tasks.size());
        assertEquals("task-1", tasks.get(0).id());
        assertEquals("Pay bills", tasks.get(0).title());
        server.verify();
    }

    @Test
    void fetchTasksReturnsEmptyListWhenTasksApiIsDisabled() {
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

        server.expect(requestTo("https://tasks.googleapis.com/tasks/v1/users/@me/lists"))
                .andExpect(header("Authorization", "Bearer access-token"))
                .andRespond(withStatus(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON).body("""
                        {"error":{"status":"PERMISSION_DENIED"}}
                        """));

        GoogleTasksClientImpl client = new GoogleTasksClientImpl(properties, builder);

        var tasks = client.fetchTasks("access-token", 10);

        assertEquals(0, tasks.size());
        server.verify();
    }
}
