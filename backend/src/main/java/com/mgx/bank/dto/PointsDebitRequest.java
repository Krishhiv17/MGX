package com.mgx.bank.dto;

import java.math.BigDecimal;

public class PointsDebitRequest {
  private BigDecimal pointsAmount;

  public PointsDebitRequest() {}

  public PointsDebitRequest(BigDecimal pointsAmount) {
    this.pointsAmount = pointsAmount;
  }

  public BigDecimal getPointsAmount() {
    return pointsAmount;
  }

  public void setPointsAmount(BigDecimal pointsAmount) {
    this.pointsAmount = pointsAmount;
  }
}
