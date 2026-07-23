package com.familyos.familyos.integrations.google.gmail;

import java.util.List;

public interface GoogleGmailMailboxAdapter {
    GoogleGmailMailboxSyncResult fetchNewMessages(String accessToken, String historyCursor, int maxResults);
    List<GoogleGmailNormalizedMessage> fetchMessages(String accessToken, int maxResults, String query);
    String insertMessage(String accessToken, String rawMimeBase64Url, List<String> labelIds);
    void trashMessage(String accessToken, String messageId);
}
