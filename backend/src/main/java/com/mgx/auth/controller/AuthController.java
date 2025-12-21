package com.mgx.auth.controller;

import com.mgx.auth.dto.AuthResponse;
import com.mgx.auth.dto.CurrentUserResponse;
import com.mgx.auth.dto.LoginRequest;
import com.mgx.auth.dto.RegisterRequest;
import com.mgx.auth.security.JwtUserPrincipal;
import com.mgx.auth.service.AuthService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public AuthResponse register(@RequestBody RegisterRequest request) {
    return authService.register(request.getEmail(), request.getPassword(), request.getRole());
  }

  @PostMapping("/login")
  public AuthResponse login(@RequestBody LoginRequest request) {
    return authService.login(request.getEmail(), request.getPassword());
  }

  @GetMapping("/me")
  public CurrentUserResponse me(@AuthenticationPrincipal JwtUserPrincipal principal) {
    return new CurrentUserResponse(principal.getUserId(), principal.getEmail(), principal.getRole());
  }
}
