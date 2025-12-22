package com.mgx.developer.dto;

import com.mgx.developer.model.DeveloperStatus;

public class CreateDeveloperRequest {
  private String name;
  private String settlementCurrency;
  private String bankAccountRef;
  private DeveloperStatus status;
  private String userId;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSettlementCurrency() {
    return settlementCurrency;
  }

  public void setSettlementCurrency(String settlementCurrency) {
    this.settlementCurrency = settlementCurrency;
  }

  public String getBankAccountRef() {
    return bankAccountRef;
  }

  public void setBankAccountRef(String bankAccountRef) {
    this.bankAccountRef = bankAccountRef;
  }

  public DeveloperStatus getStatus() {
    return status;
  }

  public void setStatus(DeveloperStatus status) {
    this.status = status;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }
}
