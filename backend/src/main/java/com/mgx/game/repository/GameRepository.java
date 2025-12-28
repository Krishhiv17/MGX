package com.mgx.game.repository;

import com.mgx.game.model.Game;
import com.mgx.game.model.GameStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, UUID> {
  List<Game> findByStatus(GameStatus status);
}
