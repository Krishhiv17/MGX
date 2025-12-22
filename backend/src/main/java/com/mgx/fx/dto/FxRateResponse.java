package com.mgx.fx.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

public class FxRateResponse {
  private String provider;
  private OffsetDateTime fetchedAt;
  private String base;
  private Map<String, BigDecimal> rates;

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public OffsetDateTime getFetchedAt() {
    return fetchedAt;
  }

  public void setFetchedAt(OffsetDateTime fetchedAt) {
    this.fetchedAt = fetchedAt;
  }

  public String getBase() {
    return base;
  }

  public void setBase(String base) {
    this.base = base;
  }

  public Map<String, BigDecimal> getRates() {
    return rates;
  }

  public void setRates(Map<String, BigDecimal> rates) {
    this.rates = rates;
  }
}
