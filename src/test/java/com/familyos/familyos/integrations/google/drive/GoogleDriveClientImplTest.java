package com.familyos.familyos.integrations.google.drive;

import com.familyos.familyos.config.properties.GoogleProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GoogleDriveClientImplTest {

    @Test
    void fetchFilesUsesDriveCollectionQuery() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        GoogleProperties properties = new GoogleProperties(
                new GoogleProperties.Apis(
                        "https://gmail.googleapis.com/gmail/v1",
                        "https://www.googleapis.com/calendar/v3",
                        "https://www.googleapis.com/drive/v3",
                        "https://tasks.googleapis.com/tasks/v1",
                        "https://people.googleapis.com/v1"
                ),
                new GoogleProperties.Gmail("me", 10),
                new GoogleProperties.Calendar("primary", 10),
                new GoogleProperties.Drive(10),
                new GoogleProperties.Tasks(10),
                new GoogleProperties.People(20)
        );

        server.expect(requestTo("https://www.googleapis.com/drive/v3/files?pageSize=10&corpora=user&spaces=drive&orderBy=modifiedTime%20desc&trashed=false&fields=files(id,name,mimeType,modifiedTime,webViewLink,size)"))
                .andExpect(header("Authorization", "Bearer access-token"))
                .andRespond(withSuccess("""
                        {"files":[{"id":"file-1","name":"Roadmap","mimeType":"application/vnd.google-apps.document","modifiedTime":"2026-07-03T14:00:00Z","webViewLink":"https://drive.google.com/file/d/file-1/view","size":"12345"}]}
                        """, MediaType.APPLICATION_JSON));

        GoogleDriveClientImpl client = new GoogleDriveClientImpl(properties, builder);

        var files = client.fetchFiles("access-token", 10);

        assertEquals(1, files.size());
        assertEquals("file-1", files.get(0).id());
        assertEquals("Roadmap", files.get(0).name());
        server.verify();
    }

    @Test
    void fetchFileContentDownloadsMediaBody() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        GoogleProperties properties = new GoogleProperties(
                new GoogleProperties.Apis(
                        "https://gmail.googleapis.com/gmail/v1",
                        "https://www.googleapis.com/calendar/v3",
                        "https://www.googleapis.com/drive/v3",
                        "https://tasks.googleapis.com/tasks/v1",
                        "https://people.googleapis.com/v1"
                ),
                new GoogleProperties.Gmail("me", 10),
                new GoogleProperties.Calendar("primary", 10),
                new GoogleProperties.Drive(10),
                new GoogleProperties.Tasks(10),
                new GoogleProperties.People(20)
        );

        server.expect(requestTo("https://www.googleapis.com/drive/v3/files/drive-file-1?alt=media"))
                .andExpect(header("Authorization", "Bearer access-token"))
                .andRespond(withSuccess("{\"senders\":[\"allowed@example.com\"],\"subjects\":[\"Invoice\"]}", MediaType.APPLICATION_JSON));

        GoogleDriveClientImpl client = new GoogleDriveClientImpl(properties, builder);

        String content = client.fetchFileContent("access-token", "drive-file-1");

        assertEquals("{\"senders\":[\"allowed@example.com\"],\"subjects\":[\"Invoice\"]}", content);
        server.verify();
    }

    @Test
    void createFileUploadsMultipartContent() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        GoogleProperties properties = new GoogleProperties(
                new GoogleProperties.Apis(
                        "https://gmail.googleapis.com/gmail/v1",
                        "https://www.googleapis.com/calendar/v3",
                        "https://www.googleapis.com/drive/v3",
                        "https://tasks.googleapis.com/tasks/v1",
                        "https://people.googleapis.com/v1"
                ),
                new GoogleProperties.Gmail("me", 10),
                new GoogleProperties.Calendar("primary", 10),
                new GoogleProperties.Drive(10),
                new GoogleProperties.Tasks(10),
                new GoogleProperties.People(20)
        );

        server.expect(requestTo("https://www.googleapis.com/drive/v3/files?uploadType=multipart"))
                .andExpect(header("Authorization", "Bearer access-token"))
                .andRespond(withSuccess("{\"id\":\"file-1\"}", MediaType.APPLICATION_JSON));

        GoogleDriveClientImpl client = new GoogleDriveClientImpl(properties, builder);

        String fileId = client.createFile("access-token", "familyos-gmail-allowlist.csv", "text/csv", "type,value\n");

        assertEquals("file-1", fileId);
        server.verify();
    }
}
