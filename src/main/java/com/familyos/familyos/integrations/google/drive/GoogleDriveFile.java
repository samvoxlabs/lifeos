package com.familyos.familyos.integrations.google.drive;

public record GoogleDriveFile(
        String id,
        String name,
        String mimeType,
        String modifiedTime,
        String webViewLink,
        String size
) {}
