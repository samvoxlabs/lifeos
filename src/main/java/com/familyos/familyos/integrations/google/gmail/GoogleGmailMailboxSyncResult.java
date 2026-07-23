package com.familyos.familyos.integrations.google.gmail;

import java.util.List;

public record GoogleGmailMailboxSyncResult(
        List<GoogleGmailNormalizedMessage> messages,
        String historyCursor
) {
}
