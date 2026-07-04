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
import com.familyos.familyos.dto.DriveFileDto;
import com.familyos.familyos.integrations.google.drive.GoogleDriveClient;
import com.familyos.familyos.integrations.google.drive.GoogleDriveFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DriveService {

    private static final Logger log = LoggerFactory.getLogger(DriveService.class);
    private static final String GOOGLE_PROVIDER = "google";
    private static final int DEFAULT_MAX_RESULTS = 10;

    private final UserService userService;
    private final OAuthAccountService oauthAccountService;
    private final OAuthTokenService oauthTokenService;
    private final TokenRefreshService tokenRefreshService;
    private final GoogleDriveClient googleDriveClient;
    private final GoogleProperties googleProperties;

    public DriveService(UserService userService,
                        OAuthAccountService oauthAccountService,
                        OAuthTokenService oauthTokenService,
                        TokenRefreshService tokenRefreshService,
                        GoogleDriveClient googleDriveClient,
                        GoogleProperties googleProperties) {
        this.userService = userService;
        this.oauthAccountService = oauthAccountService;
        this.oauthTokenService = oauthTokenService;
        this.tokenRefreshService = tokenRefreshService;
        this.googleDriveClient = googleDriveClient;
        this.googleProperties = googleProperties;
    }

    public List<DriveFileDto> readRecentFiles(String userId) {
        log.debug("Reading recent Drive files for user: {}", userId);

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

        int maxResults = googleProperties.drive() != null && googleProperties.drive().maxResults() != null
                ? googleProperties.drive().maxResults()
                : DEFAULT_MAX_RESULTS;

        log.debug("Calling Google Drive API for user: {}", user.getEmail());
        List<GoogleDriveFile> driveFiles = googleDriveClient.fetchFiles(accessToken, maxResults);

        log.debug("Converting {} Google Drive files to DTOs", driveFiles.size());
        return driveFiles.stream()
                .map(this::toDto)
                .toList();
    }

    private DriveFileDto toDto(GoogleDriveFile file) {
        return new DriveFileDto(
                file.id(),
                file.name(),
                file.mimeType(),
                file.modifiedTime(),
                file.webViewLink(),
                file.size()
        );
    }
}
