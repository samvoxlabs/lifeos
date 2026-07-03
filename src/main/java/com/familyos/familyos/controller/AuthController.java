package com.familyos.familyos.controller;

import com.familyos.familyos.authentication.service.AuthenticationService;
import com.familyos.familyos.dto.AuthenticatedUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

  private final AuthenticationService authenticationService;

  public AuthController(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  @GetMapping("/")
  public String home() {
    return "LifeOS Gmail POC is running";
  }

  @GetMapping("/user")
  public AuthenticatedUser user() {
    return authenticationService.currentUser();
  }
}
