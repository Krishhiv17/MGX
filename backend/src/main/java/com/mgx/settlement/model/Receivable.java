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
@Table(name = "receivables")
public class Receivable {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "purchase_id", nullable = false)
  private UUID purchaseId;

  @Column(name = "developer_id", nullable = false)
  private UUID developerId;

  @Column(name = "amount_due", nullable = false, precision = 38, scale = 12)
  private BigDecimal amountDue;

  @Column(name = "settlement_currency", nullable = false)
  private String settlementCurrency;

  @Column(name = "fx_window_id")
  private UUID fxWindowId;

  @Column(name = "fx_rate_used", precision = 38, scale = 12)
  private BigDecimal fxRateUsed;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private ReceivableStatus status;

  @Column(name = "reserved_at")
  private OffsetDateTime reservedAt;

  @Column(name = "settled_at")
  private OffsetDateTime settledAt;

  @Column(name = "settlement_batch_id")
  private UUID settlementBatchId;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getPurchaseId() {
    return purchaseId;
  }

  public void setPurchaseId(UUID purchaseId) {
    this.purchaseId = purchaseId;
  }

  public UUID getDeveloperId() {
    return developerId;
  }

  public void setDeveloperId(UUID developerId) {
    this.developerId = developerId;
  }

  public BigDecimal getAmountDue() {
    return amountDue;
  }

  public void setAmountDue(BigDecimal amountDue) {
    this.amountDue = amountDue;
  }

  public String getSettlementCurrency() {
    return settlementCurrency;
  }

  public void setSettlementCurrency(String settlementCurrency) {
    this.settlementCurrency = settlementCurrency;
  }

  public UUID getFxWindowId() {
    return fxWindowId;
  }

  public void setFxWindowId(UUID fxWindowId) {
    this.fxWindowId = fxWindowId;
  }

  public BigDecimal getFxRateUsed() {
    return fxRateUsed;
  }

  public void setFxRateUsed(BigDecimal fxRateUsed) {
    this.fxRateUsed = fxRateUsed;
  }

  public ReceivableStatus getStatus() {
    return status;
  }

  public void setStatus(ReceivableStatus status) {
    this.status = status;
  }

  public OffsetDateTime getReservedAt() {
    return reservedAt;
  }

  public void setReservedAt(OffsetDateTime reservedAt) {
    this.reservedAt = reservedAt;
  }

  public OffsetDateTime getSettledAt() {
    return settledAt;
  }

  public void setSettledAt(OffsetDateTime settledAt) {
    this.settledAt = settledAt;
  }

  public UUID getSettlementBatchId() {
    return settlementBatchId;
  }

  public void setSettlementBatchId(UUID settlementBatchId) {
    this.settlementBatchId = settlementBatchId;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
