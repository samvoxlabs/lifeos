package com.familyos.familyos.integrations.google.drive;

import com.familyos.familyos.config.properties.GoogleProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class GoogleDriveClientImpl implements GoogleDriveClient {

    private static final Logger log = LoggerFactory.getLogger(GoogleDriveClientImpl.class);

    private final RestClient restClient;

    public GoogleDriveClientImpl(GoogleProperties googleProperties) {
        this.restClient = RestClient.builder()
                .baseUrl(googleProperties.apis().driveBaseUrl())
                .build();
    }

    @Override
    public List<GoogleDriveFile> fetchFiles(String accessToken, int maxResults) {
        log.debug("Fetching {} files from Google Drive API", maxResults);

        Map<String, Object> response = restClient.get()
                .uri("/files?pageSize={pageSize}&orderBy=modifiedTime desc&fields=files(id,name,mimeType,modifiedTime,webViewLink,size)", maxResults)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        List<Map<String, Object>> files = (List<Map<String, Object>>) response.getOrDefault("files", List.of());
        log.debug("Retrieved {} files from Google Drive API", files.size());

        return files.stream()
                .map(this::toFile)
                .toList();
    }

    private GoogleDriveFile toFile(Map<String, Object> file) {
        return new GoogleDriveFile(
                (String) file.get("id"),
                (String) file.getOrDefault("name", ""),
                (String) file.getOrDefault("mimeType", ""),
                (String) file.getOrDefault("modifiedTime", ""),
                (String) file.getOrDefault("webViewLink", ""),
                file.get("size") == null ? "" : String.valueOf(file.get("size"))
        );
    }
}
