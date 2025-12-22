package com.mgx.rates.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class CreateMgcUgcRateRequest {
  private UUID gameId;
  private BigDecimal ugcPerMgc;
  private OffsetDateTime activeFrom;

  public UUID getGameId() {
    return gameId;
  }

  public void setGameId(UUID gameId) {
    this.gameId = gameId;
  }

  public BigDecimal getUgcPerMgc() {
    return ugcPerMgc;
  }

  public void setUgcPerMgc(BigDecimal ugcPerMgc) {
    this.ugcPerMgc = ugcPerMgc;
  }

  public OffsetDateTime getActiveFrom() {
    return activeFrom;
  }

  public void setActiveFrom(OffsetDateTime activeFrom) {
    this.activeFrom = activeFrom;
  }
}
