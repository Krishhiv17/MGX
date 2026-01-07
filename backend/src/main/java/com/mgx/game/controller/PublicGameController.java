package com.mgx.game.controller;

import com.mgx.auth.security.JwtUserPrincipal;
import com.mgx.game.dto.GameResponse;
import com.mgx.game.model.Game;
import com.mgx.game.model.GameStatus;
import com.mgx.game.repository.GameRepository;
import com.mgx.game.service.GameCountryService;
import com.mgx.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/games")
public class PublicGameController {
  private final GameRepository gameRepository;
  private final UserRepository userRepository;
  private final GameCountryService gameCountryService;

  public PublicGameController(
    GameRepository gameRepository,
    UserRepository userRepository,
    GameCountryService gameCountryService
  ) {
    this.gameRepository = gameRepository;
    this.userRepository = userRepository;
    this.gameCountryService = gameCountryService;
  }

  @GetMapping
  @PreAuthorize("hasRole('USER')")
  public List<GameResponse> listActiveGames(@AuthenticationPrincipal JwtUserPrincipal principal) {
    if (principal == null) {
      throw new AccessDeniedException("Access denied");
    }
    String countryCode = userRepository.findById(principal.getUserId())
      .map(user -> user.getCountryCode())
      .orElseThrow(() -> new AccessDeniedException("Access denied"));
    List<Game> games = gameRepository.findByStatusAndCountryCode(
      GameStatus.ACTIVE.name(),
      countryCode
    );
    Map<UUID, List<String>> allowedMap = gameCountryService.getAllowedCountriesForGames(
      games.stream().map(Game::getId).collect(Collectors.toList())
    );
    return games.stream()
      .map(game -> GameResponse.from(game, allowedMap.get(game.getId())))
      .collect(Collectors.toList());
  }

  @GetMapping("/{gameId}")
  @PreAuthorize("hasRole('USER')")
  public GameResponse getGame(
    @AuthenticationPrincipal JwtUserPrincipal principal,
    @PathVariable UUID gameId
  ) {
    if (principal == null) {
      throw new AccessDeniedException("Access denied");
    }
    String countryCode = userRepository.findById(principal.getUserId())
      .map(user -> user.getCountryCode())
      .orElseThrow(() -> new AccessDeniedException("Access denied"));
    Game game = gameRepository.findById(gameId)
      .orElseThrow(() -> new IllegalArgumentException("Game not found"));
    if (!gameCountryService.isAllowed(gameId, countryCode)) {
      throw new IllegalArgumentException("Game not available in your country");
    }
    List<String> allowedCountries = gameCountryService.getAllowedCountries(gameId);
    return GameResponse.from(game, allowedCountries);
  }
}
