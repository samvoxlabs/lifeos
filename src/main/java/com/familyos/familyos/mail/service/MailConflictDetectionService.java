package com.familyos.familyos.mail.service;

import com.familyos.familyos.authentication.entity.OAuthAccount;
import com.familyos.familyos.mail.entity.MailExtractedEvent;
import com.familyos.familyos.mail.model.DetectedConflict;

import java.util.List;

public interface MailConflictDetectionService {
    List<DetectedConflict> detectConflicts(OAuthAccount account, MailExtractedEvent event, String accessToken);
}
