package com.familyos.familyos.api.dto;

public record StartResponse(
    String status,
    ConnectionStatusResponse google,
    ConnectionStatusResponse llm
) {}
