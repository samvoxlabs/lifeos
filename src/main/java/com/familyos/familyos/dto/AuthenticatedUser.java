package com.familyos.familyos.dto;

public record AuthenticatedUser(
    String id,
    String email,
    String name,
    String provider
) {}
