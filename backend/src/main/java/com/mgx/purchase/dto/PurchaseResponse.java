package com.mgx.purchase.dto;

import com.mgx.purchase.model.Purchase;
import com.mgx.purchase.model.PurchaseStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class PurchaseResponse {
  private UUID id;
  private UUID userId;
  private UUID gameId;
  private BigDecimal mgcSpent;
  private BigDecimal ugcCredited;
  private BigDecimal rateUgcPerMgcSnapshot;
  private UUID rateId;
  private PurchaseStatus status;
  private String idempotencyKey;
  private OffsetDateTime createdAt;

  public static PurchaseResponse from(Purchase purchase) {
    PurchaseResponse response = new PurchaseResponse();
    response.setId(purchase.getId());
    response.setUserId(purchase.getUserId());
    response.setGameId(purchase.getGameId());
    response.setMgcSpent(purchase.getMgcSpent());
    response.setUgcCredited(purchase.getUgcCredited());
    response.setRateUgcPerMgcSnapshot(purchase.getRateUgcPerMgcSnapshot());
    response.setRateId(purchase.getRateId());
    response.setStatus(purchase.getStatus());
    response.setIdempotencyKey(purchase.getIdempotencyKey());
    response.setCreatedAt(purchase.getCreatedAt());
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

  public UUID getGameId() {
    return gameId;
  }

  public void setGameId(UUID gameId) {
    this.gameId = gameId;
  }

  public BigDecimal getMgcSpent() {
    return mgcSpent;
  }

  public void setMgcSpent(BigDecimal mgcSpent) {
    this.mgcSpent = mgcSpent;
  }

  public BigDecimal getUgcCredited() {
    return ugcCredited;
  }

  public void setUgcCredited(BigDecimal ugcCredited) {
    this.ugcCredited = ugcCredited;
  }

  public BigDecimal getRateUgcPerMgcSnapshot() {
    return rateUgcPerMgcSnapshot;
  }

  public void setRateUgcPerMgcSnapshot(BigDecimal rateUgcPerMgcSnapshot) {
    this.rateUgcPerMgcSnapshot = rateUgcPerMgcSnapshot;
  }

  public UUID getRateId() {
    return rateId;
  }

  public void setRateId(UUID rateId) {
    this.rateId = rateId;
  }

  public PurchaseStatus getStatus() {
    return status;
  }

  public void setStatus(PurchaseStatus status) {
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
