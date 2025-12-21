package com.mgx.topup.model;

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
@Table(name = "topups")
public class Topup {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "points_debited", nullable = false, precision = 38, scale = 12)
  private BigDecimal pointsDebited;

  @Column(name = "mgc_credited", nullable = false, precision = 38, scale = 12)
  private BigDecimal mgcCredited;

  @Column(name = "rate_points_per_mgc_snapshot", nullable = false, precision = 38, scale = 12)
  private BigDecimal ratePointsPerMgcSnapshot;

  @Column(name = "rate_id")
  private UUID rateId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private TopupStatus status;

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
