package com.familyos.familyos.common;

import java.time.Instant;
import java.util.UUID;

public record ApiResponse<T>(T data, ApiMeta meta) {
    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(data, new ApiMeta(UUID.randomUUID().toString(), Instant.now().toString()));
    }
}
