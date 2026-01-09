package com.mgx.rates.controller;

import com.mgx.auth.security.JwtUserPrincipal;
import com.mgx.developer.model.Developer;
import com.mgx.developer.model.DeveloperStatus;
import com.mgx.developer.repository.DeveloperRepository;
import com.mgx.game.model.Game;
import com.mgx.game.model.GameStatus;
import com.mgx.game.repository.GameRepository;
import com.mgx.rates.dto.CreateMgcUgcRateRequest;
import com.mgx.rates.dto.MgcUgcRateResponse;
import com.mgx.rates.model.RateMgcUgc;
import com.mgx.rates.service.RateService;
import com.mgx.rates.repository.RateMgcUgcRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/developer/rates")
public class DeveloperRateController {
  private final RateService rateService;
  private final GameRepository gameRepository;
  private final DeveloperRepository developerRepository;
  private final RateMgcUgcRepository rateRepository;

  public DeveloperRateController(
    RateService rateService,
    GameRepository gameRepository,
    DeveloperRepository developerRepository,
    RateMgcUgcRepository rateRepository
  ) {
    this.rateService = rateService;
    this.gameRepository = gameRepository;
    this.developerRepository = developerRepository;
    this.rateRepository = rateRepository;
  }

  @GetMapping("/mgc-ugc")
  @PreAuthorize("hasRole('DEVELOPER')")
  public List<MgcUgcRateResponse> listRates(@AuthenticationPrincipal JwtUserPrincipal principal) {
    Developer developer = developerRepository.findTopByUserIdOrderByCreatedAtDesc(principal.getUserId())
      .orElseThrow(() -> new IllegalArgumentException("Developer not linked to user"));
    List<UUID> gameIds = gameRepository.findByDeveloperId(developer.getId()).stream()
      .map(Game::getId)
      .collect(Collectors.toList());
    if (gameIds.isEmpty()) {
      return List.of();
    }
    return rateRepository.findByGameIdIn(gameIds).stream()
      .map(MgcUgcRateResponse::from)
      .collect(Collectors.toList());
  }

  @PostMapping("/mgc-ugc")
  @PreAuthorize("hasRole('DEVELOPER')")
  public MgcUgcRateResponse proposeRate(
    @AuthenticationPrincipal JwtUserPrincipal principal,
    @RequestBody CreateMgcUgcRateRequest request
  ) {
    if (request.getGameId() == null) {
      throw new IllegalArgumentException("gameId is required");
    }
    Developer developer = developerRepository
      .findTopByUserIdAndStatusOrderByCreatedAtDesc(principal.getUserId(), DeveloperStatus.ACTIVE)
      .orElseThrow(() -> new IllegalArgumentException("Developer is not active"));
    Game game = gameRepository.findById(request.getGameId())
      .orElseThrow(() -> new IllegalArgumentException("Game not found"));
    if (!game.getDeveloperId().equals(developer.getId())) {
      throw new IllegalArgumentException("Game does not belong to developer");
    }
    if (game.getStatus() != GameStatus.ACTIVE) {
      throw new IllegalArgumentException("Game is not active");
    }

    RateMgcUgc rate = rateService.proposeMgcUgcRate(
      request.getGameId(),
      request.getUgcPerMgc(),
      request.getActiveFrom(),
      principal.getUserId()
    );
    return MgcUgcRateResponse.from(rate);
  }
}
