package com.mgx.rates.dto;

import com.mgx.rates.model.RateMgcUgc;
import com.mgx.rates.model.RateStatus;
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
  private RateStatus status;
  private UUID approvedBy;
  private OffsetDateTime approvedAt;
  private String rejectionReason;
  private OffsetDateTime createdAt;

  public static MgcUgcRateResponse from(RateMgcUgc rate) {
    MgcUgcRateResponse response = new MgcUgcRateResponse();
    response.setId(rate.getId());
    response.setGameId(rate.getGameId());
    response.setUgcPerMgc(rate.getUgcPerMgc());
    response.setActiveFrom(rate.getActiveFrom());
    response.setActiveTo(rate.getActiveTo());
    response.setCreatedBy(rate.getCreatedBy());
    response.setStatus(rate.getStatus());
    response.setApprovedBy(rate.getApprovedBy());
    response.setApprovedAt(rate.getApprovedAt());
    response.setRejectionReason(rate.getRejectionReason());
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

  public RateStatus getStatus() {
    return status;
  }

  public void setStatus(RateStatus status) {
    this.status = status;
  }

  public UUID getApprovedBy() {
    return approvedBy;
  }

  public void setApprovedBy(UUID approvedBy) {
    this.approvedBy = approvedBy;
  }

  public OffsetDateTime getApprovedAt() {
    return approvedAt;
  }

  public void setApprovedAt(OffsetDateTime approvedAt) {
    this.approvedAt = approvedAt;
  }

  public String getRejectionReason() {
    return rejectionReason;
  }

  public void setRejectionReason(String rejectionReason) {
    this.rejectionReason = rejectionReason;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
