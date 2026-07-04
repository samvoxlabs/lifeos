package com.familyos.familyos.integrations.google.people;

public record GoogleContact(
        String resourceName,
        String displayName,
        String email,
        String phoneNumber
) {}
