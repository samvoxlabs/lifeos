package com.familyos.familyos.integrations.google.drive;

import java.util.List;

public interface GoogleDriveClient {
    List<GoogleDriveFile> fetchFiles(String accessToken, int maxResults);
    List<GoogleDriveFile> fetchFiles(String accessToken, int maxResults, String query);
    String fetchFileContent(String accessToken, String fileId);
    String createFile(String accessToken, String name, String mimeType, String content);
}
