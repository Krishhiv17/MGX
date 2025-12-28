package com.mgx.game.controller;

import com.mgx.game.dto.GameResponse;
import com.mgx.game.model.Game;
import com.mgx.game.model.GameStatus;
import com.mgx.game.repository.GameRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/games")
public class PublicGameController {
  private final GameRepository gameRepository;

  public PublicGameController(GameRepository gameRepository) {
    this.gameRepository = gameRepository;
  }

  @GetMapping
  @PreAuthorize("hasRole('USER')")
  public List<GameResponse> listActiveGames() {
    return gameRepository.findByStatus(GameStatus.ACTIVE)
      .stream()
      .map(GameResponse::from)
      .collect(Collectors.toList());
  }

  @GetMapping("/{gameId}")
  @PreAuthorize("hasRole('USER')")
  public GameResponse getGame(@PathVariable UUID gameId) {
    Game game = gameRepository.findById(gameId)
      .orElseThrow(() -> new IllegalArgumentException("Game not found"));
    return GameResponse.from(game);
  }
}
