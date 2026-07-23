package com.familyos.familyos.mail.service;

import com.familyos.familyos.mail.entity.MailMessage;
import com.familyos.familyos.mail.model.ExtractedMailEventCandidate;

import java.util.Optional;

public interface MailEventExtractionService {
    Optional<ExtractedMailEventCandidate> extractFromMessage(MailMessage message);
}
