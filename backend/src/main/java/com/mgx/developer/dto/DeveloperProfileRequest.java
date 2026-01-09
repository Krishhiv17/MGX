package com.mgx.developer.dto;

public class DeveloperProfileRequest {
  private String name;
  private String settlementCurrency;
  private String bankAccountRef;

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
}
