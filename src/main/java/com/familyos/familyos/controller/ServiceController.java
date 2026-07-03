package com.familyos.familyos.controller;

import com.familyos.familyos.dto.GmailMessageDto;
import com.familyos.familyos.service.GmailService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/gmail")
public class ServiceController {

    private final GmailService gmailService;

    public ServiceController(GmailService gmailService) {
        this.gmailService = gmailService;
    }

    @GetMapping("/messages")
    public List<GmailMessageDto> messages(@AuthenticationPrincipal String userEmail) {
        return gmailService.readLatestMessages(userEmail);
    }
}
