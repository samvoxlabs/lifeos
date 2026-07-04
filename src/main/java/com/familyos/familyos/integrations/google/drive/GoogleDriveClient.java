package com.familyos.familyos.integrations.google.drive;

import java.util.List;

public interface GoogleDriveClient {
    List<GoogleDriveFile> fetchFiles(String accessToken, int maxResults);
}
