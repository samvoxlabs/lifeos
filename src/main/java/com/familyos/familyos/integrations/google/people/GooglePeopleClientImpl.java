package com.familyos.familyos.integrations.google.people;

import com.familyos.familyos.config.properties.GoogleProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class GooglePeopleClientImpl implements GooglePeopleClient {

    private static final Logger log = LoggerFactory.getLogger(GooglePeopleClientImpl.class);

    private final RestClient restClient;

    public GooglePeopleClientImpl(GoogleProperties googleProperties) {
        this.restClient = RestClient.builder()
                .baseUrl(googleProperties.apis().peopleBaseUrl())
                .build();
    }

    @Override
    public List<GoogleContact> fetchContacts(String accessToken, int maxResults) {
        log.debug("Fetching {} contacts from Google People API", maxResults);

        Map<String, Object> response = restClient.get()
                .uri("/people/me/connections?pageSize={pageSize}&personFields=names,emailAddresses,phoneNumbers", maxResults)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        List<Map<String, Object>> items = (List<Map<String, Object>>) response.getOrDefault("connections", List.of());
        log.debug("Retrieved {} contacts from Google People API", items.size());

        return items.stream()
                .map(this::toContact)
                .toList();
    }

    private GoogleContact toContact(Map<String, Object> contact) {
        return new GoogleContact(
                (String) contact.getOrDefault("resourceName", ""),
                firstValue(contact.get("names"), "displayName"),
                firstValue(contact.get("emailAddresses"), "value"),
                firstValue(contact.get("phoneNumbers"), "value")
        );
    }

    private String firstValue(Object field, String key) {
        if (!(field instanceof List<?> list) || list.isEmpty()) {
            return "";
        }

        Object first = list.getFirst();
        if (!(first instanceof Map<?, ?> map)) {
            return "";
        }

        Object value = map.get(key);
        return value == null ? "" : String.valueOf(value);
    }
}
