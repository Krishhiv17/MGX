package com.mgx.fx.dto;

import java.math.BigDecimal;
import java.util.Map;

public class FxCurrentResponse {
  private FxWindowResponse window;
  private String baseCurrency;
  private Map<String, BigDecimal> rates;

  public FxCurrentResponse() {}

  public FxCurrentResponse(FxWindowResponse window, String baseCurrency, Map<String, BigDecimal> rates) {
    this.window = window;
    this.baseCurrency = baseCurrency;
    this.rates = rates;
  }

  public FxWindowResponse getWindow() {
    return window;
  }

  public void setWindow(FxWindowResponse window) {
    this.window = window;
  }

  public String getBaseCurrency() {
    return baseCurrency;
  }

  public void setBaseCurrency(String baseCurrency) {
    this.baseCurrency = baseCurrency;
  }

  public Map<String, BigDecimal> getRates() {
    return rates;
  }

  public void setRates(Map<String, BigDecimal> rates) {
    this.rates = rates;
  }
}
