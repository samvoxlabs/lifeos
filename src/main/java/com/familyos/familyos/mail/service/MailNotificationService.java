package com.familyos.familyos.mail.service;

import com.familyos.familyos.mail.entity.MailConflict;
import com.familyos.familyos.mail.entity.MailExtractedEvent;

public interface MailNotificationService {
    void sendResolutionNotification(MailConflict conflict, MailExtractedEvent event, String recipientEmail, String actionKey);
}
