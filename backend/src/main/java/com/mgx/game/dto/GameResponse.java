package com.mgx.game.dto;

import com.mgx.game.model.Game;
import com.mgx.game.model.GameStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public class GameResponse {
  private UUID id;
  private UUID developerId;
  private String developerName;
  private String name;
  private GameStatus status;
  private String settlementCurrency;
  private OffsetDateTime createdAt;

  public static GameResponse from(Game game) {
    GameResponse response = new GameResponse();
    response.setId(game.getId());
    response.setDeveloperId(game.getDeveloperId());
    response.setDeveloperName(game.getDeveloperName());
    response.setName(game.getName());
    response.setStatus(game.getStatus());
    response.setSettlementCurrency(game.getSettlementCurrency());
    response.setCreatedAt(game.getCreatedAt());
    return response;
  }

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

  public String getDeveloperName() {
    return developerName;
  }

  public void setDeveloperName(String developerName) {
    this.developerName = developerName;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public GameStatus getStatus() {
    return status;
  }

  public void setStatus(GameStatus status) {
    this.status = status;
  }

  public String getSettlementCurrency() {
    return settlementCurrency;
  }

  public void setSettlementCurrency(String settlementCurrency) {
    this.settlementCurrency = settlementCurrency;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
