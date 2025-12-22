package com.mgx.topup.dto;

import java.math.BigDecimal;

public class TopupRequest {
  private BigDecimal pointsAmount;
  private BigDecimal mgcAmount;

  public BigDecimal getPointsAmount() {
    return pointsAmount;
  }

  public void setPointsAmount(BigDecimal pointsAmount) {
    this.pointsAmount = pointsAmount;
  }

  public BigDecimal getMgcAmount() {
    return mgcAmount;
  }

  public void setMgcAmount(BigDecimal mgcAmount) {
    this.mgcAmount = mgcAmount;
  }
}
