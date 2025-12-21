package com.mgx.auth.security;

import com.mgx.user.model.UserRole;
import java.util.UUID;

public class JwtUserPrincipal {
  private final UUID userId;
  private final String email;
  private final UserRole role;

  public JwtUserPrincipal(UUID userId, String email, UserRole role) {
    this.userId = userId;
    this.email = email;
    this.role = role;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getEmail() {
    return email;
  }

  public UserRole getRole() {
    return role;
  }
}
