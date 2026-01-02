package com.mgx.admin.dto;

import com.mgx.purchase.model.Purchase;
import com.mgx.settlement.model.Receivable;
import com.mgx.topup.model.Topup;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class AdminTransactionResponse {
  private UUID id;
  private String type;
  private UUID userId;
  private UUID developerId;
  private UUID gameId;
  private BigDecimal pointsDebited;
  private BigDecimal mgcCredited;
  private BigDecimal mgcSpent;
  private BigDecimal ugcCredited;
  private BigDecimal amountDue;
  private String settlementCurrency;
  private String status;
  private OffsetDateTime createdAt;

  public static AdminTransactionResponse fromTopup(Topup topup) {
    AdminTransactionResponse response = new AdminTransactionResponse();
    response.setId(topup.getId());
    response.setType("TOPUP");
    response.setUserId(topup.getUserId());
    response.setPointsDebited(topup.getPointsDebited());
    response.setMgcCredited(topup.getMgcCredited());
    response.setStatus(topup.getStatus().name());
    response.setCreatedAt(topup.getCreatedAt());
    return response;
  }

  public static AdminTransactionResponse fromPurchase(Purchase purchase) {
    AdminTransactionResponse response = new AdminTransactionResponse();
    response.setId(purchase.getId());
    response.setType("PURCHASE");
    response.setUserId(purchase.getUserId());
    response.setGameId(purchase.getGameId());
    response.setMgcSpent(purchase.getMgcSpent());
    response.setUgcCredited(purchase.getUgcCredited());
    response.setStatus(purchase.getStatus().name());
    response.setCreatedAt(purchase.getCreatedAt());
    return response;
  }

  public static AdminTransactionResponse fromReceivable(Receivable receivable) {
    AdminTransactionResponse response = new AdminTransactionResponse();
    response.setId(receivable.getId());
    response.setType("RECEIVABLE");
    response.setDeveloperId(receivable.getDeveloperId());
    response.setAmountDue(receivable.getAmountDue());
    response.setSettlementCurrency(receivable.getSettlementCurrency());
    response.setStatus(receivable.getStatus().name());
    response.setCreatedAt(receivable.getCreatedAt());
    return response;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public UUID getDeveloperId() {
    return developerId;
  }

  public void setDeveloperId(UUID developerId) {
    this.developerId = developerId;
  }

  public UUID getGameId() {
    return gameId;
  }

  public void setGameId(UUID gameId) {
    this.gameId = gameId;
  }

  public BigDecimal getPointsDebited() {
    return pointsDebited;
  }

  public void setPointsDebited(BigDecimal pointsDebited) {
    this.pointsDebited = pointsDebited;
  }

  public BigDecimal getMgcCredited() {
    return mgcCredited;
  }

  public void setMgcCredited(BigDecimal mgcCredited) {
    this.mgcCredited = mgcCredited;
  }

  public BigDecimal getMgcSpent() {
    return mgcSpent;
  }

  public void setMgcSpent(BigDecimal mgcSpent) {
    this.mgcSpent = mgcSpent;
  }

  public BigDecimal getUgcCredited() {
    return ugcCredited;
  }

  public void setUgcCredited(BigDecimal ugcCredited) {
    this.ugcCredited = ugcCredited;
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

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
