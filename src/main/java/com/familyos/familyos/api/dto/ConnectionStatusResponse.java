package com.familyos.familyos.api.dto;

public record ConnectionStatusResponse(
    boolean configured,
    boolean connected,
    String message
) {}
