package com.mgx.game.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class GameAllowedCountryId implements Serializable {
  private UUID gameId;
  private String countryCode;

  public GameAllowedCountryId() {}

  public GameAllowedCountryId(UUID gameId, String countryCode) {
    this.gameId = gameId;
    this.countryCode = countryCode;
  }

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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GameAllowedCountryId that = (GameAllowedCountryId) o;
    return Objects.equals(gameId, that.gameId)
      && Objects.equals(countryCode, that.countryCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(gameId, countryCode);
  }
}
