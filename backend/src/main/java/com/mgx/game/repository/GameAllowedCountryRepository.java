package com.mgx.game.repository;

import com.mgx.game.model.GameAllowedCountry;
import com.mgx.game.model.GameAllowedCountryId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameAllowedCountryRepository
  extends JpaRepository<GameAllowedCountry, GameAllowedCountryId> {
  List<GameAllowedCountry> findByGameId(UUID gameId);

  List<GameAllowedCountry> findByGameIdIn(List<UUID> gameIds);

  boolean existsByGameIdAndCountryCode(UUID gameId, String countryCode);

  void deleteByGameId(UUID gameId);
}
