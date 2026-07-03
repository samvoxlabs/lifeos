package com.familyos.familyos.controller;

import com.familyos.familyos.authentication.service.AuthenticationService;
import com.familyos.familyos.dto.GmailMessageDto;
import com.familyos.familyos.service.GmailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/gmail")
public class ServiceController {

    private final GmailService gmailService;
    private final AuthenticationService authenticationService;

    public ServiceController(GmailService gmailService, AuthenticationService authenticationService) {
        this.gmailService = gmailService;
        this.authenticationService = authenticationService;
    }

    @GetMapping("/messages")
    public List<GmailMessageDto> messages() {
        return gmailService.readLatestMessages(authenticationService.currentUser().id());
    }
}
