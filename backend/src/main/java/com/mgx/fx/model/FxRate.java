package com.mgx.fx.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "fx_rates")
@IdClass(FxRateId.class)
public class FxRate {

  @Id
  @Column(name = "window_id", nullable = false)
  private UUID windowId;

  @Id
  @Column(name = "quote_currency", nullable = false)
  private String quoteCurrency;

  @Column(name = "base_currency", nullable = false)
  private String baseCurrency;

  @Column(name = "rate", nullable = false, precision = 38, scale = 12)
  private BigDecimal rate;

  public UUID getWindowId() {
    return windowId;
  }

  public void setWindowId(UUID windowId) {
    this.windowId = windowId;
  }

  public String getQuoteCurrency() {
    return quoteCurrency;
  }

  public void setQuoteCurrency(String quoteCurrency) {
    this.quoteCurrency = quoteCurrency;
  }

  public String getBaseCurrency() {
    return baseCurrency;
  }

  public void setBaseCurrency(String baseCurrency) {
    this.baseCurrency = baseCurrency;
  }

  public BigDecimal getRate() {
    return rate;
  }

  public void setRate(BigDecimal rate) {
    this.rate = rate;
  }
}
