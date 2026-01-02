package com.mgx.profile.dto;

import com.mgx.user.model.User;
import com.mgx.user.model.UserRole;
import java.time.OffsetDateTime;
import java.util.UUID;

public class ProfileResponse {
  private UUID userId;
  private String email;
  private UserRole role;
  private String phoneNumber;
  private String countryCode;
  private OffsetDateTime phoneVerifiedAt;

  public ProfileResponse(
    UUID userId,
    String email,
    UserRole role,
    String phoneNumber,
    String countryCode,
    OffsetDateTime phoneVerifiedAt
  ) {
    this.userId = userId;
    this.email = email;
    this.role = role;
    this.phoneNumber = phoneNumber;
    this.countryCode = countryCode;
    this.phoneVerifiedAt = phoneVerifiedAt;
  }

  public static ProfileResponse from(User user) {
    return new ProfileResponse(
      user.getId(),
      user.getEmail(),
      user.getRole(),
      user.getPhoneNumber(),
      user.getCountryCode(),
      user.getPhoneVerifiedAt()
    );
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

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public OffsetDateTime getPhoneVerifiedAt() {
    return phoneVerifiedAt;
  }

  public void setPhoneVerifiedAt(OffsetDateTime phoneVerifiedAt) {
    this.phoneVerifiedAt = phoneVerifiedAt;
  }
}
