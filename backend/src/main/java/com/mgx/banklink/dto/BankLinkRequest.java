package com.mgx.banklink.dto;

import java.util.UUID;

public class BankLinkRequest {
  private String bankRef;
  private String phoneNumber;
  private UUID otpSessionId;

  public String getBankRef() {
    return bankRef;
  }

  public void setBankRef(String bankRef) {
    this.bankRef = bankRef;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public UUID getOtpSessionId() {
    return otpSessionId;
  }

  public void setOtpSessionId(UUID otpSessionId) {
    this.otpSessionId = otpSessionId;
  }
}
