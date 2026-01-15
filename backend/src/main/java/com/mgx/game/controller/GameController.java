package com.mgx.game.controller;

import com.mgx.auth.security.JwtUserPrincipal;
import com.mgx.common.dto.RejectRequest;
import com.mgx.common.util.ValidationUtil;
import com.mgx.developer.model.Developer;
import com.mgx.developer.repository.DeveloperRepository;
import com.mgx.game.dto.CreateGameRequest;
import com.mgx.game.dto.GameResponse;
import com.mgx.game.dto.UpdateGameStatusRequest;
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
  private final GameCountryService gameCountryService;

  public GameController(
    GameRepository gameRepository,
    DeveloperRepository developerRepository,
    GameCountryService gameCountryService
  ) {
    this.gameRepository = gameRepository;
    this.developerRepository = developerRepository;
    this.gameCountryService = gameCountryService;
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Transactional
  public GameResponse createGame(
    @AuthenticationPrincipal JwtUserPrincipal principal,
    @RequestBody CreateGameRequest request
  ) {
    if (request.getDeveloperId() == null) {
      throw new IllegalArgumentException("developerId is required");
    }
    Developer developer = developerRepository.findById(request.getDeveloperId())
      .orElseThrow(() -> new IllegalArgumentException("Developer not found"));
    String settlementCurrency = developer.getSettlementCurrency();
    ValidationUtil.requireCurrency(settlementCurrency);
    if (request.getSettlementCurrency() != null &&
      !settlementCurrency.equalsIgnoreCase(request.getSettlementCurrency())) {
      throw new IllegalArgumentException("Settlement currency must match developer currency");
    }
    List<String> allowedCountries = gameCountryService.normalizeAndValidate(
      request.getAllowedCountries()
    );

    Game game = new Game();
    game.setDeveloperId(request.getDeveloperId());
    game.setDeveloperName(developer.getName());
    game.setName(request.getName());
    game.setSettlementCurrency(settlementCurrency);
    game.setStatus(GameStatus.ACTIVE);
    game.setApprovedBy(principal.getUserId());
    game.setApprovedAt(java.time.OffsetDateTime.now());

    Game saved = gameRepository.saveAndFlush(game);
    gameCountryService.setAllowedCountries(saved.getId(), allowedCountries);
    return GameResponse.from(saved, allowedCountries);
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public List<GameResponse> listGames() {
    List<Game> games = gameRepository.findAll();
    Map<UUID, List<String>> allowedMap = gameCountryService.getAllowedCountriesForGames(
      games.stream().map(Game::getId).collect(Collectors.toList())
    );
    return games.stream()
      .map(game -> GameResponse.from(game, allowedMap.get(game.getId())))
      .collect(Collectors.toList());
  }

  @GetMapping("/{gameId}")
  @PreAuthorize("hasRole('ADMIN')")
  public GameResponse getGame(@PathVariable UUID gameId) {
    Game game = gameRepository.findById(gameId)
      .orElseThrow(() -> new IllegalArgumentException("Game not found"));
    List<String> allowedCountries = gameCountryService.getAllowedCountries(gameId);
    return GameResponse.from(game, allowedCountries);
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
    Game saved = gameRepository.save(game);
    List<String> allowedCountries = gameCountryService.getAllowedCountries(gameId);
    return GameResponse.from(saved, allowedCountries);
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
    game.setRejectionReason(null);
    Game saved = gameRepository.save(game);
    List<String> allowedCountries = gameCountryService.getAllowedCountries(gameId);
    return GameResponse.from(saved, allowedCountries);
  }

  @PostMapping("/{gameId}/reject")
  @PreAuthorize("hasRole('ADMIN')")
  public GameResponse rejectGame(
    @AuthenticationPrincipal JwtUserPrincipal principal,
    @PathVariable UUID gameId,
    @RequestBody RejectRequest request
  ) {
    ValidationUtil.requireNonBlank(request.getReason(), "reason");
    Game game = gameRepository.findById(gameId)
      .orElseThrow(() -> new IllegalArgumentException("Game not found"));
    game.setStatus(GameStatus.REJECTED);
    game.setApprovedBy(principal.getUserId());
    game.setApprovedAt(java.time.OffsetDateTime.now());
    game.setRejectionReason(request.getReason());
    Game saved = gameRepository.save(game);
    List<String> allowedCountries = gameCountryService.getAllowedCountries(gameId);
    return GameResponse.from(saved, allowedCountries);
  }
}
