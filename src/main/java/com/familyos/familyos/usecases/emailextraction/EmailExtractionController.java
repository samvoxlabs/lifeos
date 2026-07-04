package com.familyos.familyos.usecases.emailextraction;

import com.familyos.familyos.authentication.service.AuthenticationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/llm/email")
public class EmailExtractionController {

  private final AuthenticationService authenticationService;
  private final EmailExtractionService emailExtractionService;

  public EmailExtractionController(
    AuthenticationService authenticationService,
    EmailExtractionService emailExtractionService
  ) {
    this.authenticationService = authenticationService;
    this.emailExtractionService = emailExtractionService;
  }

  @PostMapping("/extract")
  public EmailExtractionResponse extract(@RequestBody(required = false) EmailExtractionRequest request) {
    return emailExtractionService.extract(authenticationService.currentUser().id(), request);
  }
}
