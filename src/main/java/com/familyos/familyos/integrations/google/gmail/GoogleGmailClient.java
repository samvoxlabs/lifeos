package com.familyos.familyos.integrations.google.gmail;

import java.util.List;

public interface GoogleGmailClient {
    List<GoogleGmailMessage> fetchMessages(String accessToken, int maxResults);
    List<GoogleGmailMessage> fetchMessages(String accessToken, int maxResults, String query);
}
