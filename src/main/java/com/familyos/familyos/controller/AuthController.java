package com.familyos.familyos.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthController {

  @GetMapping("/")
  public String home() {
    return "LifeOS Gmail POC is running";
  }

  @GetMapping("/user")
  public Map<String, Object> user(@AuthenticationPrincipal OAuth2User user) {
    return user.getAttributes();
  }
}
