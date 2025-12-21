package com.mgx.auth.dto;

import com.mgx.user.model.UserRole;
import java.util.UUID;

public class CurrentUserResponse {
  private UUID userId;
  private String email;
  private UserRole role;

  public CurrentUserResponse() {}

  public CurrentUserResponse(UUID userId, String email, UserRole role) {
    this.userId = userId;
    this.email = email;
    this.role = role;
  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public UserRole getRole() {
    return role;
  }

  public void setRole(UserRole role) {
    this.role = role;
  }
}
