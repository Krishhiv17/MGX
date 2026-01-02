package com.mgx.rates.model;

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
@Table(name = "rate_mgc_ugc")
public class RateMgcUgc {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "game_id", nullable = false)
  private UUID gameId;

  @Column(name = "ugc_per_mgc", nullable = false, precision = 38, scale = 12)
  private BigDecimal ugcPerMgc;

  @Column(name = "active_from", nullable = false)
  private OffsetDateTime activeFrom;

  @Column(name = "active_to")
  private OffsetDateTime activeTo;

  @Column(name = "created_by")
  private UUID createdBy;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private RateStatus status;

  @Column(name = "approved_by")
  private UUID approvedBy;

  @Column(name = "approved_at")
  private OffsetDateTime approvedAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

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

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
