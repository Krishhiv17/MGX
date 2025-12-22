package com.mgx.settlement.dto;

import com.mgx.settlement.model.SettlementBatch;
import com.mgx.settlement.model.SettlementBatchStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class SettlementBatchResponse {
  private UUID id;
  private SettlementBatchStatus status;
  private String currency;
  private BigDecimal totalAmount;
  private OffsetDateTime requestedAt;
  private OffsetDateTime processedAt;
  private String failureReason;
  private List<ReceivableResponse> receivables;

  public static SettlementBatchResponse from(SettlementBatch batch, List<ReceivableResponse> receivables) {
    SettlementBatchResponse response = new SettlementBatchResponse();
    response.setId(batch.getId());
    response.setStatus(batch.getStatus());
    response.setCurrency(batch.getCurrency());
    response.setTotalAmount(batch.getTotalAmount());
    response.setRequestedAt(batch.getRequestedAt());
    response.setProcessedAt(batch.getProcessedAt());
    response.setFailureReason(batch.getFailureReason());
    response.setReceivables(receivables);
    return response;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
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

  public List<ReceivableResponse> getReceivables() {
    return receivables;
  }

  public void setReceivables(List<ReceivableResponse> receivables) {
    this.receivables = receivables;
  }
}
