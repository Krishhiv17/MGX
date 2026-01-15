package com.mgx.admin.controller;

import com.mgx.admin.dto.AdminCreateUserRequest;
import com.mgx.auth.dto.AuthResponse;
import com.mgx.auth.service.AuthService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/users")
public class AdminUserController {
  private final AuthService authService;

  public AdminUserController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/admins")
  @PreAuthorize("hasRole('ADMIN')")
  public AuthResponse createAdmin(@RequestBody AdminCreateUserRequest request) {
    return authService.registerAdmin(request.getEmail(), request.getPassword());
  }
}
