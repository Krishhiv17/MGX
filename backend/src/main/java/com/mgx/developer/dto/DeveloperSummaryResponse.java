package com.mgx.developer.dto;

import com.mgx.developer.model.Developer;
import com.mgx.developer.model.DeveloperStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class DeveloperSummaryResponse {
  private UUID id;
  private String name;
  private String settlementCurrency;
  private String bankAccountRef;
  private DeveloperStatus status;
  private UUID userId;
  private UUID approvedBy;
  private OffsetDateTime approvedAt;
  private OffsetDateTime createdAt;
  private BigDecimal unsettledTotal;

  public static DeveloperSummaryResponse from(Developer developer, BigDecimal unsettledTotal) {
    DeveloperSummaryResponse response = new DeveloperSummaryResponse();
    response.setId(developer.getId());
    response.setName(developer.getName());
    response.setSettlementCurrency(developer.getSettlementCurrency());
    response.setBankAccountRef(developer.getBankAccountRef());
    response.setStatus(developer.getStatus());
    response.setUserId(developer.getUserId());
    response.setApprovedBy(developer.getApprovedBy());
    response.setApprovedAt(developer.getApprovedAt());
    response.setCreatedAt(developer.getCreatedAt());
    response.setUnsettledTotal(unsettledTotal);
    return response;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSettlementCurrency() {
    return settlementCurrency;
  }

  public void setSettlementCurrency(String settlementCurrency) {
    this.settlementCurrency = settlementCurrency;
  }

  public String getBankAccountRef() {
    return bankAccountRef;
  }

  public void setBankAccountRef(String bankAccountRef) {
    this.bankAccountRef = bankAccountRef;
  }

  public DeveloperStatus getStatus() {
    return status;
  }

  public void setStatus(DeveloperStatus status) {
    this.status = status;
  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
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

  public BigDecimal getUnsettledTotal() {
    return unsettledTotal;
  }

  public void setUnsettledTotal(BigDecimal unsettledTotal) {
    this.unsettledTotal = unsettledTotal;
  }
}
