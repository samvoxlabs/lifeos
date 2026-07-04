package com.familyos.familyos.controller;

import com.familyos.familyos.authentication.service.AuthenticationService;
import com.familyos.familyos.dto.GmailAllowlistDto;
import com.familyos.familyos.service.GmailAllowlistService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gmail/allowlist")
public class GmailAllowlistController {

    private final GmailAllowlistService gmailAllowlistService;
    private final AuthenticationService authenticationService;

    public GmailAllowlistController(GmailAllowlistService gmailAllowlistService, AuthenticationService authenticationService) {
        this.gmailAllowlistService = gmailAllowlistService;
        this.authenticationService = authenticationService;
    }

    @GetMapping
    public GmailAllowlistDto get() {
        return gmailAllowlistService.readAllowlist(authenticationService.currentUser().id());
    }
}
