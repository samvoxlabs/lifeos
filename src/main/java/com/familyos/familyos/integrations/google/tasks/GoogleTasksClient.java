package com.familyos.familyos.integrations.google.tasks;

import java.util.List;

public interface GoogleTasksClient {
    List<GoogleTaskItem> fetchTasks(String accessToken, int maxResults);
}
