package com.mgx.common.dto;

import java.time.OffsetDateTime;

public class ApiError {
  private String message;
  private String code;
  private OffsetDateTime timestamp;

  public ApiError() {}

  public ApiError(String message, String code, OffsetDateTime timestamp) {
    this.message = message;
    this.code = code;
    this.timestamp = timestamp;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public OffsetDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(OffsetDateTime timestamp) {
    this.timestamp = timestamp;
  }
}
