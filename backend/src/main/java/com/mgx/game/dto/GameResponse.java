package com.mgx.game.dto;

import com.mgx.game.model.Game;
import com.mgx.game.model.GameStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class GameResponse {
  private UUID id;
  private UUID developerId;
  private String developerName;
  private String name;
  private GameStatus status;
  private UUID approvedBy;
  private OffsetDateTime approvedAt;
  private String settlementCurrency;
  private OffsetDateTime createdAt;
  private List<String> allowedCountries;

  public static GameResponse from(Game game) {
    return from(game, null);
  }

  public static GameResponse from(Game game, List<String> allowedCountries) {
    GameResponse response = new GameResponse();
    response.setId(game.getId());
    response.setDeveloperId(game.getDeveloperId());
    response.setDeveloperName(game.getDeveloperName());
    response.setName(game.getName());
    response.setStatus(game.getStatus());
    response.setApprovedBy(game.getApprovedBy());
    response.setApprovedAt(game.getApprovedAt());
    response.setSettlementCurrency(game.getSettlementCurrency());
    response.setCreatedAt(game.getCreatedAt());
    response.setAllowedCountries(allowedCountries);
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

  public List<String> getAllowedCountries() {
    return allowedCountries;
  }

  public void setAllowedCountries(List<String> allowedCountries) {
    this.allowedCountries = allowedCountries;
  }
}
