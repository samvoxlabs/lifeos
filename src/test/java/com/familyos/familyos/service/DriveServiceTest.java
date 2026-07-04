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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriveServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private OAuthAccountService oauthAccountService;

    @Mock
    private OAuthTokenService oauthTokenService;

    @Mock
    private TokenRefreshService tokenRefreshService;

    @Mock
    private GoogleDriveClient googleDriveClient;

    private DriveService driveService;

    @BeforeEach
    void setUp() {
        GoogleProperties properties = new GoogleProperties(
                new GoogleProperties.Apis(
                        "https://gmail.googleapis.com/gmail/v1",
                        "https://www.googleapis.com/calendar/v3",
                        "https://www.googleapis.com/drive/v3"
                ),
                new GoogleProperties.Gmail("me", 10),
                new GoogleProperties.Calendar("primary", 10),
                new GoogleProperties.Drive(10)
        );
        driveService = new DriveService(
                userService,
                oauthAccountService,
                oauthTokenService,
                tokenRefreshService,
                googleDriveClient,
                properties
        );
    }

    @Test
    void readRecentFilesMapsGoogleFilesToDtos() {
        User user = user("user@example.com");
        OAuthAccount account = new OAuthAccount(user, "google", "subject-1", "user@example.com", "Test User");
        OAuthToken token = new OAuthToken(account, "access-token", "refresh-token", "Bearer", "openid email", LocalDateTime.now().plusHours(1));

        when(userService.findById(user.getId())).thenReturn(Optional.of(user));
        when(oauthAccountService.findByUserAndProvider(user, "google")).thenReturn(Optional.of(account));
        when(oauthTokenService.findByAccount(account)).thenReturn(Optional.of(token));
        when(tokenRefreshService.getValidAccessToken(token)).thenReturn("access-token");
        when(googleDriveClient.fetchFiles("access-token", 10)).thenReturn(List.of(
                new GoogleDriveFile("file-1", "Roadmap", "application/vnd.google-apps.document", "2026-07-03T14:00:00Z", "https://drive.google.com/file/d/file-1/view", "12345")
        ));

        List<DriveFileDto> result = driveService.readRecentFiles(user.getId().toString());

        assertEquals(1, result.size());
        assertEquals("file-1", result.get(0).id());
        assertEquals("Roadmap", result.get(0).name());
        verify(googleDriveClient).fetchFiles("access-token", 10);
    }

    @Test
    void readRecentFilesThrowsWhenUserMissing() {
        UUID userId = UUID.randomUUID();
        when(userService.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> driveService.readRecentFiles(userId.toString()));
        verifyNoInteractions(oauthAccountService, oauthTokenService, tokenRefreshService, googleDriveClient);
    }

    @Test
    void readRecentFilesThrowsWhenTokenMissing() {
        User user = user("user@example.com");
        OAuthAccount account = new OAuthAccount(user, "google", "subject-1", "user@example.com", "Test User");
        when(userService.findById(user.getId())).thenReturn(Optional.of(user));
        when(oauthAccountService.findByUserAndProvider(user, "google")).thenReturn(Optional.of(account));
        when(oauthTokenService.findByAccount(account)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> driveService.readRecentFiles(user.getId().toString()));
        verifyNoInteractions(tokenRefreshService, googleDriveClient);
    }

    private User user(String email) {
        User user = new User(email, "Test User", "google");
        user.setId(UUID.randomUUID());
        return user;
    }
}
