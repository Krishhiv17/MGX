package com.mgx.game.dto;

import com.mgx.game.model.GameStatus;
import java.util.List;
import java.util.UUID;

public class CreateGameRequest {
  private UUID developerId;
  private String name;
  private String settlementCurrency;
  private GameStatus status;
  private List<String> allowedCountries;

  public UUID getDeveloperId() {
    return developerId;
  }

  public void setDeveloperId(UUID developerId) {
    this.developerId = developerId;
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

  public GameStatus getStatus() {
    return status;
  }

  public void setStatus(GameStatus status) {
    this.status = status;
  }

  public List<String> getAllowedCountries() {
    return allowedCountries;
  }

  public void setAllowedCountries(List<String> allowedCountries) {
    this.allowedCountries = allowedCountries;
  }
}
