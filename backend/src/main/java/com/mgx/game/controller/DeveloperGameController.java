package com.mgx.game.controller;

import com.mgx.auth.security.JwtUserPrincipal;
import com.mgx.common.util.ValidationUtil;
import com.mgx.developer.model.Developer;
import com.mgx.developer.model.DeveloperStatus;
import com.mgx.developer.repository.DeveloperRepository;
import com.mgx.game.dto.DeveloperGameRequest;
import com.mgx.game.dto.GameResponse;
import com.mgx.game.model.Game;
import com.mgx.game.model.GameStatus;
import com.mgx.game.repository.GameRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/developer/games")
public class DeveloperGameController {
  private final GameRepository gameRepository;
  private final DeveloperRepository developerRepository;

  public DeveloperGameController(GameRepository gameRepository, DeveloperRepository developerRepository) {
    this.gameRepository = gameRepository;
    this.developerRepository = developerRepository;
  }

  @PostMapping
  @PreAuthorize("hasRole('DEVELOPER')")
  public GameResponse createGame(
    @AuthenticationPrincipal JwtUserPrincipal principal,
    @RequestBody DeveloperGameRequest request
  ) {
    ValidationUtil.requireCurrency(request.getSettlementCurrency());
    if (request.getName() == null || request.getName().isBlank()) {
      throw new IllegalArgumentException("name is required");
    }

    Developer developer = developerRepository.findByUserId(principal.getUserId())
      .orElseThrow(() -> new IllegalArgumentException("Developer not linked to user"));
    if (developer.getStatus() != DeveloperStatus.ACTIVE) {
      throw new IllegalArgumentException("Developer is not active");
    }

    Game game = new Game();
    game.setDeveloperId(developer.getId());
    game.setDeveloperName(developer.getName());
    game.setName(request.getName());
    game.setSettlementCurrency(request.getSettlementCurrency());
    game.setStatus(GameStatus.PENDING_APPROVAL);
    return GameResponse.from(gameRepository.save(game));
  }

  @GetMapping
  @PreAuthorize("hasRole('DEVELOPER')")
  public List<GameResponse> listGames(@AuthenticationPrincipal JwtUserPrincipal principal) {
    Developer developer = developerRepository.findByUserId(principal.getUserId())
      .orElseThrow(() -> new IllegalArgumentException("Developer not linked to user"));
    return gameRepository.findByDeveloperId(developer.getId())
      .stream()
      .map(GameResponse::from)
      .collect(Collectors.toList());
  }
}
