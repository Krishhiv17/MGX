package com.mgx.game.repository;

import com.mgx.game.model.Game;
import com.mgx.game.model.GameStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GameRepository extends JpaRepository<Game, UUID> {
  List<Game> findByStatus(GameStatus status);

  List<Game> findByDeveloperId(UUID developerId);

  @Query(
    value = """
      SELECT g.* FROM games g
      JOIN game_allowed_countries gac ON g.id = gac.game_id
      WHERE g.status = :status
        AND gac.country_code = :countryCode
      """,
    nativeQuery = true
  )
  List<Game> findByStatusAndCountryCode(
    @Param("status") String status,
    @Param("countryCode") String countryCode
  );
}
