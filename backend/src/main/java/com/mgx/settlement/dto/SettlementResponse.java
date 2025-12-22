package com.mgx.settlement.dto;

import com.mgx.settlement.model.SettlementBatch;
import com.mgx.settlement.model.SettlementBatchStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class SettlementResponse {
  private UUID batchId;
  private SettlementBatchStatus status;
  private BigDecimal totalAmount;
  private String currency;
  private OffsetDateTime requestedAt;

  public static SettlementResponse from(SettlementBatch batch) {
    SettlementResponse response = new SettlementResponse();
    response.setBatchId(batch.getId());
    response.setStatus(batch.getStatus());
    response.setTotalAmount(batch.getTotalAmount());
    response.setCurrency(batch.getCurrency());
    response.setRequestedAt(batch.getRequestedAt());
    return response;
  }

  public UUID getBatchId() {
    return batchId;
  }

  public void setBatchId(UUID batchId) {
    this.batchId = batchId;
  }

  public SettlementBatchStatus getStatus() {
    return status;
  }

  public void setStatus(SettlementBatchStatus status) {
    this.status = status;
  }

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(BigDecimal totalAmount) {
    this.totalAmount = totalAmount;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public OffsetDateTime getRequestedAt() {
    return requestedAt;
  }

  public void setRequestedAt(OffsetDateTime requestedAt) {
    this.requestedAt = requestedAt;
  }
}
