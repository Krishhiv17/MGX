package com.mgx.otp.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class OtpResponse {
  private UUID sessionId;
  private OffsetDateTime expiresAt;
  private String debugCode;

  public OtpResponse(UUID sessionId, OffsetDateTime expiresAt, String debugCode) {
    this.sessionId = sessionId;
    this.expiresAt = expiresAt;
    this.debugCode = debugCode;
  }

  public UUID getSessionId() {
    return sessionId;
  }

  public void setSessionId(UUID sessionId) {
    this.sessionId = sessionId;
  }

  public OffsetDateTime getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(OffsetDateTime expiresAt) {
    this.expiresAt = expiresAt;
  }

  public String getDebugCode() {
    return debugCode;
  }

  public void setDebugCode(String debugCode) {
    this.debugCode = debugCode;
  }
}
