package com.mgx.game.repository;

import com.mgx.game.model.Game;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, UUID> {}
