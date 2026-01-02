package com.mgx.banklink.dto;

import com.mgx.banklink.model.BankLink;
import java.time.OffsetDateTime;
import java.util.UUID;

public class BankLinkResponse {
  private UUID id;
  private UUID userId;
  private String bankRef;
  private String phoneNumber;
  private OffsetDateTime verifiedAt;
  private OffsetDateTime createdAt;

  public static BankLinkResponse from(BankLink link) {
    BankLinkResponse response = new BankLinkResponse();
    response.setId(link.getId());
    response.setUserId(link.getUserId());
    response.setBankRef(link.getBankRef());
    response.setPhoneNumber(link.getPhoneNumber());
    response.setVerifiedAt(link.getVerifiedAt());
    response.setCreatedAt(link.getCreatedAt());
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

  public String getBankRef() {
    return bankRef;
  }

  public void setBankRef(String bankRef) {
    this.bankRef = bankRef;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public OffsetDateTime getVerifiedAt() {
    return verifiedAt;
  }

  public void setVerifiedAt(OffsetDateTime verifiedAt) {
    this.verifiedAt = verifiedAt;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
