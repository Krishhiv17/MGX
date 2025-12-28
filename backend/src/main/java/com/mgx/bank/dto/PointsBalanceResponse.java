package com.mgx.bank.dto;

import java.math.BigDecimal;

public class PointsBalanceResponse {
  private String userId;
  private BigDecimal pointsAvailable;

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public BigDecimal getPointsAvailable() {
    return pointsAvailable;
  }

  public void setPointsAvailable(BigDecimal pointsAvailable) {
    this.pointsAvailable = pointsAvailable;
  }
}
