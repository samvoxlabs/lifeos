package com.familyos.familyos.integrations.google.tasks;

import com.familyos.familyos.config.properties.GoogleProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class GoogleTasksClientImpl implements GoogleTasksClient {

    private static final Logger log = LoggerFactory.getLogger(GoogleTasksClientImpl.class);

    private final RestClient restClient;

    public GoogleTasksClientImpl(GoogleProperties googleProperties, RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl(googleProperties.apis().tasksBaseUrl())
                .build();
    }

    @Override
    public List<GoogleTaskItem> fetchTasks(String accessToken, int maxResults) {
        log.debug("Fetching {} tasks from Google Tasks API", maxResults);
        String taskListId = resolveTaskListId(accessToken);
        if (taskListId == null) {
            return List.of();
        }

        Map<String, Object> response;
        try {
            response = restClient.get()
                    .uri("/users/@me/lists/{taskListId}/tasks?maxResults={maxResults}&showCompleted=true&showHidden=false",
                            taskListId, maxResults)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(Map.class);
        } catch (HttpClientErrorException.Forbidden ex) {
            log.warn("Google Tasks API returned 403 while reading tasks; returning no tasks");
            return List.of();
        }

        List<Map<String, Object>> items = (List<Map<String, Object>>) response.getOrDefault("items", List.of());
        log.debug("Retrieved {} tasks from Google Tasks API", items.size());

        return items.stream()
                .map(this::toTask)
                .toList();
    }

    private String resolveTaskListId(String accessToken) {
        Map<String, Object> response;
        try {
            response = restClient.get()
                    .uri("/users/@me/lists")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(Map.class);
        } catch (HttpClientErrorException.Forbidden ex) {
            log.warn("Google Tasks API is unavailable for this project or user; returning no tasks");
            return null;
        }

        List<Map<String, Object>> items = (List<Map<String, Object>>) response.getOrDefault("items", List.of());
        if (items.isEmpty()) {
            return null;
        }

        Object taskListId = items.getFirst().get("id");
        if (!(taskListId instanceof String id) || id.isBlank()) {
            return null;
        }

        return id;
    }

    private GoogleTaskItem toTask(Map<String, Object> item) {
        return new GoogleTaskItem(
                (String) item.get("id"),
                (String) item.getOrDefault("title", ""),
                (String) item.getOrDefault("notes", ""),
                (String) item.getOrDefault("status", ""),
                (String) item.getOrDefault("due", ""),
                (String) item.getOrDefault("updated", "")
        );
    }
}
