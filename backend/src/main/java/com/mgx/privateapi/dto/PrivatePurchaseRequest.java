package com.mgx.privateapi.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class PrivatePurchaseRequest {
  private UUID gameId;
  private BigDecimal mgcAmount;
  private BigDecimal ugcAmount;
  private String phoneNumber;
  private String bankRef;
  private UUID otpSessionId;

  public UUID getGameId() {
    return gameId;
  }

  public void setGameId(UUID gameId) {
    this.gameId = gameId;
  }

  public BigDecimal getMgcAmount() {
    return mgcAmount;
  }

  public void setMgcAmount(BigDecimal mgcAmount) {
    this.mgcAmount = mgcAmount;
  }

  public BigDecimal getUgcAmount() {
    return ugcAmount;
  }

  public void setUgcAmount(BigDecimal ugcAmount) {
    this.ugcAmount = ugcAmount;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public String getBankRef() {
    return bankRef;
  }

  public void setBankRef(String bankRef) {
    this.bankRef = bankRef;
  }

  public UUID getOtpSessionId() {
    return otpSessionId;
  }

  public void setOtpSessionId(UUID otpSessionId) {
    this.otpSessionId = otpSessionId;
  }
}
