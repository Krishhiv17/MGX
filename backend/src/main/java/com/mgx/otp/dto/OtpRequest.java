package com.mgx.otp.dto;

import com.mgx.otp.model.OtpPurpose;

public class OtpRequest {
  private String phoneNumber;
  private OtpPurpose purpose;

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public OtpPurpose getPurpose() {
    return purpose;
  }

  public void setPurpose(OtpPurpose purpose) {
    this.purpose = purpose;
  }
}
