package com.familyos.familyos.integrations.google.drive;

import com.familyos.familyos.config.properties.GoogleProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class GoogleDriveClientImpl implements GoogleDriveClient {

    private static final Logger log = LoggerFactory.getLogger(GoogleDriveClientImpl.class);
    private static final String MULTIPART_BOUNDARY = "FamilyOSDriveBoundary";

    private final RestClient restClient;

    public GoogleDriveClientImpl(GoogleProperties googleProperties, RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl(googleProperties.apis().driveBaseUrl())
                .build();
    }

    @Override
    public List<GoogleDriveFile> fetchFiles(String accessToken, int maxResults) {
        return fetchFiles(accessToken, maxResults, null);
    }

    @Override
    public List<GoogleDriveFile> fetchFiles(String accessToken, int maxResults, String query) {
        log.debug("Fetching {} files from Google Drive API", maxResults);

        Map<String, Object> response = restClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                            .path("/files")
                            .queryParam("pageSize", maxResults)
                            .queryParam("corpora", "user")
                            .queryParam("spaces", "drive")
                            .queryParam("orderBy", "modifiedTime desc")
                            .queryParam("trashed", "false")
                            .queryParam("fields", "files(id,name,mimeType,modifiedTime,webViewLink,size)");
                    if (query != null && !query.isBlank()) {
                        builder.queryParam("q", query);
                    }
                    return builder.build();
                })
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        List<Map<String, Object>> files = (List<Map<String, Object>>) response.getOrDefault("files", List.of());
        log.debug("Retrieved {} files from Google Drive API", files.size());

        return files.stream()
                .map(this::toFile)
                .toList();
    }

    @Override
    public String fetchFileContent(String accessToken, String fileId) {
        log.debug("Fetching Drive file content for file: {}", fileId);
        return restClient.get()
                .uri("/files/{fileId}?alt=media", fileId)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(String.class);
    }

    @Override
    public String createFile(String accessToken, String name, String mimeType, String content) {
        log.debug("Creating Drive file: {}", name);

        String multipartBody = "--" + MULTIPART_BOUNDARY + "\r\n"
                + "Content-Type: application/json; charset=UTF-8\r\n\r\n"
                + "{\"name\":\"" + name + "\",\"mimeType\":\"" + mimeType + "\"}\r\n"
                + "--" + MULTIPART_BOUNDARY + "\r\n"
                + "Content-Type: " + mimeType + "\r\n\r\n"
                + content + "\r\n"
                + "--" + MULTIPART_BOUNDARY + "--\r\n";

        Map<String, Object> response = restClient.post()
                .uri("/files?uploadType=multipart")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.parseMediaType("multipart/related; boundary=" + MULTIPART_BOUNDARY))
                .body(multipartBody)
                .retrieve()
                .body(Map.class);

        return response == null ? "" : String.valueOf(response.getOrDefault("id", ""));
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
