package com.mgx.fx.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class FxRateId implements Serializable {
  private UUID windowId;
  private String quoteCurrency;

  public FxRateId() {}

  public FxRateId(UUID windowId, String quoteCurrency) {
    this.windowId = windowId;
    this.quoteCurrency = quoteCurrency;
  }

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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FxRateId fxRateId = (FxRateId) o;
    return Objects.equals(windowId, fxRateId.windowId)
      && Objects.equals(quoteCurrency, fxRateId.quoteCurrency);
  }

  @Override
  public int hashCode() {
    return Objects.hash(windowId, quoteCurrency);
  }
}
