package com.mgx.purchase.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "purchases")
public class Purchase {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "game_id", nullable = false)
  private UUID gameId;

  @Column(name = "mgc_spent", nullable = false, precision = 38, scale = 12)
  private BigDecimal mgcSpent;

  @Column(name = "ugc_credited", nullable = false, precision = 38, scale = 12)
  private BigDecimal ugcCredited;

  @Column(name = "rate_ugc_per_mgc_snapshot", nullable = false, precision = 38, scale = 12)
  private BigDecimal rateUgcPerMgcSnapshot;

  @Column(name = "rate_id")
  private UUID rateId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private PurchaseStatus status;

  @Column(name = "idempotency_key", nullable = false)
  private String idempotencyKey;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

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
