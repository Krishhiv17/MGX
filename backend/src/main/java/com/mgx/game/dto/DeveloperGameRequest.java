package com.mgx.game.dto;

import java.util.List;

public class DeveloperGameRequest {
  private String name;
  private String settlementCurrency;
  private List<String> allowedCountries;

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

  public List<String> getAllowedCountries() {
    return allowedCountries;
  }

  public void setAllowedCountries(List<String> allowedCountries) {
    this.allowedCountries = allowedCountries;
  }
}
