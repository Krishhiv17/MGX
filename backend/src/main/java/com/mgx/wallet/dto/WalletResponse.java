package com.mgx.wallet.dto;

import com.mgx.wallet.model.Wallet;
import com.mgx.wallet.model.WalletType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class WalletResponse {
  private UUID id;
  private WalletType type;
  private UUID gameId;
  private String gameName;
  private BigDecimal balance;
  private OffsetDateTime createdAt;

  public static WalletResponse from(Wallet wallet, String gameName) {
    WalletResponse response = new WalletResponse();
    response.setId(wallet.getId());
    response.setType(wallet.getType());
    response.setGameId(wallet.getGameId());
    response.setGameName(gameName);
    response.setBalance(wallet.getBalance());
    response.setCreatedAt(wallet.getCreatedAt());
    return response;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public WalletType getType() {
    return type;
  }

  public void setType(WalletType type) {
    this.type = type;
  }

  public UUID getGameId() {
    return gameId;
  }

  public void setGameId(UUID gameId) {
    this.gameId = gameId;
  }

  public String getGameName() {
    return gameName;
  }

  public void setGameName(String gameName) {
    this.gameName = gameName;
  }

  public BigDecimal getBalance() {
    return balance;
  }

  public void setBalance(BigDecimal balance) {
    this.balance = balance;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
