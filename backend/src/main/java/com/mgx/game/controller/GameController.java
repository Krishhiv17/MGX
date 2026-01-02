package com.mgx.game.controller;

import com.mgx.auth.security.JwtUserPrincipal;
import com.mgx.common.util.ValidationUtil;
import com.mgx.developer.model.Developer;
import com.mgx.developer.repository.DeveloperRepository;
import com.mgx.game.dto.CreateGameRequest;
import com.mgx.game.dto.GameResponse;
import com.mgx.game.dto.UpdateGameStatusRequest;
import com.mgx.game.model.Game;
import com.mgx.game.model.GameStatus;
import com.mgx.game.repository.GameRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/games")
public class GameController {
  private final GameRepository gameRepository;
  private final DeveloperRepository developerRepository;

  public GameController(GameRepository gameRepository, DeveloperRepository developerRepository) {
    this.gameRepository = gameRepository;
    this.developerRepository = developerRepository;
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public GameResponse createGame(
    @AuthenticationPrincipal JwtUserPrincipal principal,
    @RequestBody CreateGameRequest request
  ) {
    if (request.getDeveloperId() == null) {
      throw new IllegalArgumentException("developerId is required");
    }
    Developer developer = developerRepository.findById(request.getDeveloperId())
      .orElseThrow(() -> new IllegalArgumentException("Developer not found"));
    ValidationUtil.requireCurrency(request.getSettlementCurrency());

    Game game = new Game();
    game.setDeveloperId(request.getDeveloperId());
    game.setDeveloperName(developer.getName());
    game.setName(request.getName());
    game.setSettlementCurrency(request.getSettlementCurrency());
    game.setStatus(GameStatus.ACTIVE);
    game.setApprovedBy(principal.getUserId());
    game.setApprovedAt(java.time.OffsetDateTime.now());

    return GameResponse.from(gameRepository.save(game));
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public List<GameResponse> listGames() {
    return gameRepository.findAll().stream().map(GameResponse::from).collect(Collectors.toList());
  }

  @GetMapping("/{gameId}")
  @PreAuthorize("hasRole('ADMIN')")
  public GameResponse getGame(@PathVariable UUID gameId) {
    Game game = gameRepository.findById(gameId)
      .orElseThrow(() -> new IllegalArgumentException("Game not found"));
    return GameResponse.from(game);
  }

  @PutMapping("/{gameId}/status")
  @PreAuthorize("hasRole('ADMIN')")
  public GameResponse updateStatus(
    @PathVariable UUID gameId,
    @RequestBody UpdateGameStatusRequest request
  ) {
    Game game = gameRepository.findById(gameId)
      .orElseThrow(() -> new IllegalArgumentException("Game not found"));

    if (request.getStatus() == null) {
      throw new IllegalArgumentException("status is required");
    }

    game.setStatus(request.getStatus());
    return GameResponse.from(gameRepository.save(game));
  }

  @PostMapping("/{gameId}/approve")
  @PreAuthorize("hasRole('ADMIN')")
  public GameResponse approveGame(
    @AuthenticationPrincipal JwtUserPrincipal principal,
    @PathVariable UUID gameId
  ) {
    Game game = gameRepository.findById(gameId)
      .orElseThrow(() -> new IllegalArgumentException("Game not found"));
    game.setStatus(GameStatus.ACTIVE);
    game.setApprovedBy(principal.getUserId());
    game.setApprovedAt(java.time.OffsetDateTime.now());
    return GameResponse.from(gameRepository.save(game));
  }

  @PostMapping("/{gameId}/reject")
  @PreAuthorize("hasRole('ADMIN')")
  public GameResponse rejectGame(
    @AuthenticationPrincipal JwtUserPrincipal principal,
    @PathVariable UUID gameId
  ) {
    Game game = gameRepository.findById(gameId)
      .orElseThrow(() -> new IllegalArgumentException("Game not found"));
    game.setStatus(GameStatus.REJECTED);
    game.setApprovedBy(principal.getUserId());
    game.setApprovedAt(java.time.OffsetDateTime.now());
    return GameResponse.from(gameRepository.save(game));
  }
}
