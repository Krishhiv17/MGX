package com.mgx.otp.dto;

import java.util.UUID;

public class OtpVerifyRequest {
  private UUID sessionId;
  private String code;

  public UUID getSessionId() {
    return sessionId;
  }

  public void setSessionId(UUID sessionId) {
    this.sessionId = sessionId;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }
}
