package com.familyos.familyos.mail.service;

import com.familyos.familyos.mail.api.dto.MailConflictResolveResponse;
import com.familyos.familyos.mail.api.dto.MailMessagePatchRequest;
import com.familyos.familyos.mail.api.dto.MailMessageResponse;
import com.familyos.familyos.mail.api.dto.MailMessagesPageResponse;
import com.familyos.familyos.mail.api.dto.MailResolutionRequest;
import com.familyos.familyos.mail.api.dto.MailSyncRequest;
import com.familyos.familyos.mail.api.dto.MailSyncResponse;
import com.familyos.familyos.mail.api.dto.ReviewScheduleRequest;
import com.familyos.familyos.mail.api.dto.ReviewScheduleResponse;

public interface MailboxService {
    MailSyncResponse syncMailbox(String userId, MailSyncRequest request);
    MailMessagesPageResponse listMessages(String userId, Integer limit, String cursor);
    MailMessageResponse getMessage(String userId, String messageId);
    MailMessageResponse updateMessage(String userId, String messageId, MailMessagePatchRequest request);
    MailConflictResolveResponse resolveConflict(String userId, String conflictId, MailResolutionRequest request, String idempotencyKey);
    ReviewScheduleResponse reviewSchedule(String userId, ReviewScheduleRequest request);
}
