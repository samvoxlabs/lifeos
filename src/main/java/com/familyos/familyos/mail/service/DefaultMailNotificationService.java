package com.familyos.familyos.mail.service;

import com.familyos.familyos.mail.entity.MailConflict;
import com.familyos.familyos.mail.entity.MailExtractedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DefaultMailNotificationService implements MailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(DefaultMailNotificationService.class);

    @Override
    public void sendResolutionNotification(MailConflict conflict, MailExtractedEvent event, String recipientEmail, String actionKey) {
        log.info(
                "Notification dispatched for conflict {} action {} to {} for event {}",
                conflict.getId(),
                actionKey,
                recipientEmail,
                event.getId()
        );
    }
}
