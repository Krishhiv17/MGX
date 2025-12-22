package com.mgx.topup.dto;

import com.mgx.topup.model.Topup;
import com.mgx.topup.model.TopupStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class TopupResponse {
  private UUID id;
  private UUID userId;
  private BigDecimal pointsDebited;
  private BigDecimal mgcCredited;
  private BigDecimal ratePointsPerMgcSnapshot;
  private UUID rateId;
  private TopupStatus status;
  private String idempotencyKey;
  private OffsetDateTime createdAt;

  public static TopupResponse from(Topup topup) {
    TopupResponse response = new TopupResponse();
    response.setId(topup.getId());
    response.setUserId(topup.getUserId());
    response.setPointsDebited(topup.getPointsDebited());
    response.setMgcCredited(topup.getMgcCredited());
    response.setRatePointsPerMgcSnapshot(topup.getRatePointsPerMgcSnapshot());
    response.setRateId(topup.getRateId());
    response.setStatus(topup.getStatus());
    response.setIdempotencyKey(topup.getIdempotencyKey());
    response.setCreatedAt(topup.getCreatedAt());
    return response;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public BigDecimal getPointsDebited() {
    return pointsDebited;
  }

  public void setPointsDebited(BigDecimal pointsDebited) {
    this.pointsDebited = pointsDebited;
  }

  public BigDecimal getMgcCredited() {
    return mgcCredited;
  }

  public void setMgcCredited(BigDecimal mgcCredited) {
    this.mgcCredited = mgcCredited;
  }

  public BigDecimal getRatePointsPerMgcSnapshot() {
    return ratePointsPerMgcSnapshot;
  }

  public void setRatePointsPerMgcSnapshot(BigDecimal ratePointsPerMgcSnapshot) {
    this.ratePointsPerMgcSnapshot = ratePointsPerMgcSnapshot;
  }

  public UUID getRateId() {
    return rateId;
  }

  public void setRateId(UUID rateId) {
    this.rateId = rateId;
  }

  public TopupStatus getStatus() {
    return status;
  }

  public void setStatus(TopupStatus status) {
    this.status = status;
  }

  public String getIdempotencyKey() {
    return idempotencyKey;
  }

  public void setIdempotencyKey(String idempotencyKey) {
    this.idempotencyKey = idempotencyKey;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
