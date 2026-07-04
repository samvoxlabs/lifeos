package com.familyos.familyos.dto;

public record ContactDto(
        String resourceName,
        String displayName,
        String email,
        String phoneNumber
) {}
