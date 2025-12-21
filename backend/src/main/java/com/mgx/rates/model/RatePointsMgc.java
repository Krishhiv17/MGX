package com.mgx.rates.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "rate_points_mgc")
public class RatePointsMgc {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "points_per_mgc", nullable = false, precision = 38, scale = 12)
  private BigDecimal pointsPerMgc;

  @Column(name = "active_from", nullable = false)
  private OffsetDateTime activeFrom;

  @Column(name = "active_to")
  private OffsetDateTime activeTo;

  @Column(name = "created_by")
  private UUID createdBy;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

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
