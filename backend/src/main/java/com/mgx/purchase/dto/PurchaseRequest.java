package com.mgx.purchase.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class PurchaseRequest {
  private UUID gameId;
  private BigDecimal mgcAmount;
  private BigDecimal ugcAmount;

  public UUID getGameId() {
    return gameId;
  }

  public void setGameId(UUID gameId) {
    this.gameId = gameId;
  }

  public BigDecimal getMgcAmount() {
    return mgcAmount;
  }

  public void setMgcAmount(BigDecimal mgcAmount) {
    this.mgcAmount = mgcAmount;
  }

  public BigDecimal getUgcAmount() {
    return ugcAmount;
  }

  public void setUgcAmount(BigDecimal ugcAmount) {
    this.ugcAmount = ugcAmount;
  }
}
