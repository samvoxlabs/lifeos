package com.familyos.familyos.service;

import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.authentication.entity.OAuthToken;
import com.familyos.familyos.authentication.entity.User;
import com.familyos.familyos.authentication.exception.UnauthorizedException;
import com.familyos.familyos.authentication.service.OAuthAccountService;
import com.familyos.familyos.authentication.service.OAuthTokenService;
import com.familyos.familyos.authentication.service.TokenRefreshService;
import com.familyos.familyos.authentication.service.UserService;
import com.familyos.familyos.config.properties.GoogleProperties;
import com.familyos.familyos.dto.TaskItemDto;
import com.familyos.familyos.integrations.google.tasks.GoogleTaskItem;
import com.familyos.familyos.integrations.google.tasks.GoogleTasksClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TasksService {

    private static final Logger log = LoggerFactory.getLogger(TasksService.class);
    private static final String GOOGLE_PROVIDER = "google";
    private static final int DEFAULT_MAX_RESULTS = 10;

    private final UserService userService;
    private final OAuthAccountService oauthAccountService;
    private final OAuthTokenService oauthTokenService;
    private final TokenRefreshService tokenRefreshService;
    private final GoogleTasksClient googleTasksClient;
    private final GoogleProperties googleProperties;

    public TasksService(UserService userService,
                        OAuthAccountService oauthAccountService,
                        OAuthTokenService oauthTokenService,
                        TokenRefreshService tokenRefreshService,
                        GoogleTasksClient googleTasksClient,
                        GoogleProperties googleProperties) {
        this.userService = userService;
        this.oauthAccountService = oauthAccountService;
        this.oauthTokenService = oauthTokenService;
        this.tokenRefreshService = tokenRefreshService;
        this.googleTasksClient = googleTasksClient;
        this.googleProperties = googleProperties;
    }

    public List<TaskItemDto> readTasks(String userId) {
        log.debug("Reading tasks for user: {}", userId);

        User user;
        try {
            user = userService.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));
        } catch (IllegalArgumentException ex) {
            throw new UnauthorizedException("Authenticated user not found");
        }

        OAuthAccount oauthAccount = oauthAccountService.findByUserAndProvider(user, GOOGLE_PROVIDER)
                .orElseThrow(() -> new UnauthorizedException("Google account not connected"));

        OAuthToken oauthToken = oauthTokenService.findByAccount(oauthAccount)
                .orElseThrow(() -> new UnauthorizedException("Google token not available"));
        String accessToken = tokenRefreshService.getValidAccessToken(oauthToken);

        int maxResults = googleProperties.tasks() != null && googleProperties.tasks().maxResults() != null
                ? googleProperties.tasks().maxResults()
                : DEFAULT_MAX_RESULTS;

        log.debug("Calling Google Tasks API for user: {}", user.getEmail());
        List<GoogleTaskItem> tasks = googleTasksClient.fetchTasks(accessToken, maxResults);

        return tasks.stream()
                .map(this::toDto)
                .toList();
    }

    private TaskItemDto toDto(GoogleTaskItem task) {
        return new TaskItemDto(
                task.id(),
                task.title(),
                task.notes(),
                task.status(),
                task.due(),
                task.updated()
        );
    }
}
