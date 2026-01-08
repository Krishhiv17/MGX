package com.mgx.developer.dto;

import com.mgx.developer.model.Developer;
import com.mgx.developer.model.DeveloperStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public class DeveloperResponse {
  private UUID id;
  private UUID userId;
  private String name;
  private String settlementCurrency;
  private String bankAccountRef;
  private DeveloperStatus status;
  private UUID approvedBy;
  private OffsetDateTime approvedAt;
  private String rejectionReason;
  private OffsetDateTime createdAt;

  public static DeveloperResponse from(Developer developer) {
    DeveloperResponse response = new DeveloperResponse();
    response.setId(developer.getId());
    response.setUserId(developer.getUserId());
    response.setName(developer.getName());
    response.setSettlementCurrency(developer.getSettlementCurrency());
    response.setBankAccountRef(developer.getBankAccountRef());
    response.setStatus(developer.getStatus());
    response.setApprovedBy(developer.getApprovedBy());
    response.setApprovedAt(developer.getApprovedAt());
    response.setRejectionReason(developer.getRejectionReason());
    response.setCreatedAt(developer.getCreatedAt());
    return response;
  }

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

  public String getRejectionReason() {
    return rejectionReason;
  }

  public void setRejectionReason(String rejectionReason) {
    this.rejectionReason = rejectionReason;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
