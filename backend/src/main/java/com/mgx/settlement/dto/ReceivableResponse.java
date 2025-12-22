package com.mgx.settlement.dto;

import com.mgx.settlement.model.Receivable;
import com.mgx.settlement.model.ReceivableStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class ReceivableResponse {
  private UUID id;
  private BigDecimal amountDue;
  private String settlementCurrency;
  private ReceivableStatus status;
  private OffsetDateTime createdAt;

  public static ReceivableResponse from(Receivable receivable) {
    ReceivableResponse response = new ReceivableResponse();
    response.setId(receivable.getId());
    response.setAmountDue(receivable.getAmountDue());
    response.setSettlementCurrency(receivable.getSettlementCurrency());
    response.setStatus(receivable.getStatus());
    response.setCreatedAt(receivable.getCreatedAt());
    return response;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
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

  public ReceivableStatus getStatus() {
    return status;
  }

  public void setStatus(ReceivableStatus status) {
    this.status = status;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
