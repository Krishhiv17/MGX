package com.mgx.game.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "game_allowed_countries")
@IdClass(GameAllowedCountryId.class)
public class GameAllowedCountry {

  @Id
  @Column(name = "game_id", nullable = false)
  private UUID gameId;

  @Id
  @Column(name = "country_code", nullable = false)
  private String countryCode;

  public UUID getGameId() {
    return gameId;
  }

  public void setGameId(UUID gameId) {
    this.gameId = gameId;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }
}
