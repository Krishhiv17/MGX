package com.mgx.rates.dto;

import com.mgx.rates.model.RatePointsMgc;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class PointsMgcRateResponse {
  private UUID id;
  private BigDecimal pointsPerMgc;
  private OffsetDateTime activeFrom;
  private OffsetDateTime activeTo;
  private UUID createdBy;
  private OffsetDateTime createdAt;

  public static PointsMgcRateResponse from(RatePointsMgc rate) {
    PointsMgcRateResponse response = new PointsMgcRateResponse();
    response.setId(rate.getId());
    response.setPointsPerMgc(rate.getPointsPerMgc());
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

  public BigDecimal getPointsPerMgc() {
    return pointsPerMgc;
  }

  public void setPointsPerMgc(BigDecimal pointsPerMgc) {
    this.pointsPerMgc = pointsPerMgc;
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
