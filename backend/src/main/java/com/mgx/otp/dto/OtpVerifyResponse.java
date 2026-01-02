package com.mgx.otp.dto;

import com.mgx.otp.model.OtpStatus;

public class OtpVerifyResponse {
  private OtpStatus status;
  private String message;

  public OtpVerifyResponse(OtpStatus status, String message) {
    this.status = status;
    this.message = message;
  }

  public OtpStatus getStatus() {
    return status;
  }

  public void setStatus(OtpStatus status) {
    this.status = status;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
