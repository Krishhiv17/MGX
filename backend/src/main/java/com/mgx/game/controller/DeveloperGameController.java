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
import com.mgx.game.service.GameCountryService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
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
  private final GameCountryService gameCountryService;

  public DeveloperGameController(
    GameRepository gameRepository,
    DeveloperRepository developerRepository,
    GameCountryService gameCountryService
  ) {
    this.gameRepository = gameRepository;
    this.developerRepository = developerRepository;
    this.gameCountryService = gameCountryService;
  }

  @PostMapping
  @PreAuthorize("hasRole('DEVELOPER')")
  @Transactional
  public GameResponse createGame(
    @AuthenticationPrincipal JwtUserPrincipal principal,
    @RequestBody DeveloperGameRequest request
  ) {
    if (request.getName() == null || request.getName().isBlank()) {
      throw new IllegalArgumentException("name is required");
    }
    List<String> allowedCountries = gameCountryService.normalizeAndValidate(
      request.getAllowedCountries()
    );

    Developer developer = developerRepository
      .findTopByUserIdAndStatusOrderByCreatedAtDesc(principal.getUserId(), DeveloperStatus.ACTIVE)
      .orElseThrow(() -> new IllegalArgumentException("Developer is not active"));
    String settlementCurrency = developer.getSettlementCurrency();
    ValidationUtil.requireCurrency(settlementCurrency);
    if (request.getSettlementCurrency() != null &&
      !settlementCurrency.equalsIgnoreCase(request.getSettlementCurrency())) {
      throw new IllegalArgumentException("Settlement currency must match developer currency");
    }

    Game game = new Game();
    game.setDeveloperId(developer.getId());
    game.setDeveloperName(developer.getName());
    game.setName(request.getName());
    game.setSettlementCurrency(settlementCurrency);
    game.setStatus(GameStatus.PENDING_APPROVAL);
    Game saved = gameRepository.saveAndFlush(game);
    gameCountryService.setAllowedCountries(saved.getId(), allowedCountries);
    return GameResponse.from(saved, allowedCountries);
  }

  @GetMapping
  @PreAuthorize("hasRole('DEVELOPER')")
  public List<GameResponse> listGames(@AuthenticationPrincipal JwtUserPrincipal principal) {
    Developer developer = developerRepository
      .findTopByUserIdOrderByCreatedAtDesc(principal.getUserId())
      .orElseThrow(() -> new IllegalArgumentException("Developer not linked to user"));
    List<Game> games = gameRepository.findByDeveloperId(developer.getId());
    Map<UUID, List<String>> allowedMap = gameCountryService.getAllowedCountriesForGames(
      games.stream().map(Game::getId).collect(Collectors.toList())
    );
    return games.stream()
      .map(game -> GameResponse.from(game, allowedMap.get(game.getId())))
      .collect(Collectors.toList());
  }
}
