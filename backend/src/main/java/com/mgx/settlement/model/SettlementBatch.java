package com.mgx.settlement.model;

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
@Table(name = "settlement_batches")
public class SettlementBatch {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "developer_id", nullable = false)
  private UUID developerId;

  @Column(name = "requested_by", nullable = false)
  private UUID requestedBy;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private SettlementBatchStatus status;

  @Column(name = "currency", nullable = false)
  private String currency;

  @Column(name = "total_amount", nullable = false, precision = 38, scale = 12)
  private BigDecimal totalAmount;

  @CreationTimestamp
  @Column(name = "requested_at", nullable = false, updatable = false)
  private OffsetDateTime requestedAt;

  @Column(name = "processed_at")
  private OffsetDateTime processedAt;

  @Column(name = "failure_reason")
  private String failureReason;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getDeveloperId() {
    return developerId;
  }

  public void setDeveloperId(UUID developerId) {
    this.developerId = developerId;
  }

  public UUID getRequestedBy() {
    return requestedBy;
  }

  public void setRequestedBy(UUID requestedBy) {
    this.requestedBy = requestedBy;
  }

  public SettlementBatchStatus getStatus() {
    return status;
  }

  public void setStatus(SettlementBatchStatus status) {
    this.status = status;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(BigDecimal totalAmount) {
    this.totalAmount = totalAmount;
  }

  public OffsetDateTime getRequestedAt() {
    return requestedAt;
  }

  public void setRequestedAt(OffsetDateTime requestedAt) {
    this.requestedAt = requestedAt;
  }

  public OffsetDateTime getProcessedAt() {
    return processedAt;
  }

  public void setProcessedAt(OffsetDateTime processedAt) {
    this.processedAt = processedAt;
  }

  public String getFailureReason() {
    return failureReason;
  }

  public void setFailureReason(String failureReason) {
    this.failureReason = failureReason;
  }
}
