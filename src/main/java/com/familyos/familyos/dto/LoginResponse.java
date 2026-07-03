package com.familyos.familyos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponse(
        String token,
        @JsonProperty("user_id")
        String userId,
        String email,
        String name
) {}
