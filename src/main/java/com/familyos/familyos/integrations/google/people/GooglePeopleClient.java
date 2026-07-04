package com.familyos.familyos.integrations.google.people;

import java.util.List;

public interface GooglePeopleClient {
    List<GoogleContact> fetchContacts(String accessToken, int maxResults);
}
