package com.familyos.familyos.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "google")
public record GoogleProperties(
    Apis apis,
    Gmail gmail,
    Calendar calendar,
    Drive drive,
    Tasks tasks,
    People people
) {
    
    public record Apis(
        String gmailBaseUrl,
        String calendarBaseUrl,
        String driveBaseUrl,
        String tasksBaseUrl,
        String peopleBaseUrl
    ) {}
    
    public record Gmail(
        String userId,
        Integer maxResults
    ) {}

    public record Calendar(
        String calendarId,
        Integer maxResults
    ) {}

    public record Drive(
        Integer maxResults
    ) {}

    public record Tasks(
        Integer maxResults
    ) {}

    public record People(
        Integer maxResults
    ) {}
}
