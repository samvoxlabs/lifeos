package com.familyos.familyos.dto;

public record DriveFileDto(
        String id,
        String name,
        String mimeType,
        String modifiedTime,
        String webViewLink,
        String size
) {}
