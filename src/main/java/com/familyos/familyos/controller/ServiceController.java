package com.familyos.familyos.controller;

import com.familyos.familyos.dto.GmailMessageDto;
import com.familyos.familyos.services.GmailService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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
  public List<GmailMessageDto> messages(OAuth2AuthenticationToken authentication) {
    return gmailService.readLatestMessages(authentication);
  }
}
