package com.mgx.rates.dto;

import com.mgx.rates.model.RateMgcUgc;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class MgcUgcRateResponse {
  private UUID id;
  private UUID gameId;
  private BigDecimal ugcPerMgc;
  private OffsetDateTime activeFrom;
  private OffsetDateTime activeTo;
  private UUID createdBy;
  private OffsetDateTime createdAt;

  public static MgcUgcRateResponse from(RateMgcUgc rate) {
    MgcUgcRateResponse response = new MgcUgcRateResponse();
    response.setId(rate.getId());
    response.setGameId(rate.getGameId());
    response.setUgcPerMgc(rate.getUgcPerMgc());
    response.setActiveFrom(rate.getActiveFrom());
    response.setActiveTo(rate.getActiveTo());
    response.setCreatedBy(rate.getCreatedBy());
    response.setCreatedAt(rate.getCreatedAt());
    return response;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

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

  public OffsetDateTime getActiveTo() {
    return activeTo;
  }

  public void setActiveTo(OffsetDateTime activeTo) {
    this.activeTo = activeTo;
  }

  public UUID getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(UUID createdBy) {
    this.createdBy = createdBy;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
