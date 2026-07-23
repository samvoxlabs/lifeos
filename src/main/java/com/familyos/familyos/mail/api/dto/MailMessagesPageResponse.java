package com.familyos.familyos.mail.api.dto;

import java.util.List;

public record MailMessagesPageResponse(
        List<MailMessageResponse> items,
        String nextCursor
) {
}
