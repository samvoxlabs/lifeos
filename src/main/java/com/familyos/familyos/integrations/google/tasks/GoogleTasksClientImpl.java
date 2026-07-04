package com.familyos.familyos.integrations.google.tasks;

import com.familyos.familyos.config.properties.GoogleProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class GoogleTasksClientImpl implements GoogleTasksClient {

    private static final Logger log = LoggerFactory.getLogger(GoogleTasksClientImpl.class);

    private final RestClient restClient;

    public GoogleTasksClientImpl(GoogleProperties googleProperties) {
        this.restClient = RestClient.builder()
                .baseUrl(googleProperties.apis().tasksBaseUrl())
                .build();
    }

    @Override
    public List<GoogleTaskItem> fetchTasks(String accessToken, int maxResults) {
        log.debug("Fetching {} tasks from Google Tasks API", maxResults);

        Map<String, Object> response = restClient.get()
                .uri("/users/@me/lists/@default/tasks?maxResults={maxResults}&showCompleted=true&showHidden=false", maxResults)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        List<Map<String, Object>> items = (List<Map<String, Object>>) response.getOrDefault("items", List.of());
        log.debug("Retrieved {} tasks from Google Tasks API", items.size());

        return items.stream()
                .map(this::toTask)
                .toList();
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
