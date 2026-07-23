package com.familyos.familyos.dto;

import java.util.List;

public record GoogleAuthDetailsResponse(
    String userId,
    String provider,
    String registrationId,
    String clientId,
    String projectId,
    String projectName,
    String redirectUri,
    List<String> scopes
) {}
