package com.mgx.wallet.controller;

import com.mgx.auth.security.JwtUserPrincipal;
import com.mgx.game.model.Game;
import com.mgx.game.repository.GameRepository;
import com.mgx.wallet.dto.WalletResponse;
import com.mgx.wallet.model.Wallet;
import com.mgx.wallet.model.WalletType;
import com.mgx.wallet.service.WalletService;
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
@RequestMapping("/v1/wallets")
public class WalletController {
  private final WalletService walletService;
  private final GameRepository gameRepository;

  public WalletController(WalletService walletService, GameRepository gameRepository) {
    this.walletService = walletService;
    this.gameRepository = gameRepository;
  }

  @GetMapping
  @PreAuthorize("hasRole('USER')")
  public List<WalletResponse> getWallets(@AuthenticationPrincipal JwtUserPrincipal principal) {
    List<Wallet> wallets = walletService.getWalletsByUserId(principal.getUserId());
    Map<UUID, String> gameNames = loadGameNames(wallets);
    return wallets.stream()
      .map(wallet -> WalletResponse.from(wallet, gameNames.get(wallet.getGameId())))
      .collect(Collectors.toList());
  }

  @GetMapping("/{walletId}")
  @PreAuthorize("hasRole('USER')")
  public WalletResponse getWallet(
    @AuthenticationPrincipal JwtUserPrincipal principal,
    @PathVariable UUID walletId
  ) {
    Wallet wallet = walletService.getWalletById(walletId);
    if (!wallet.getUserId().equals(principal.getUserId())) {
      throw new AccessDeniedException("Access denied");
    }
    String gameName = null;
    if (wallet.getType() == WalletType.UGC && wallet.getGameId() != null) {
      gameName = gameRepository.findById(wallet.getGameId())
        .map(Game::getName)
        .orElse(null);
    }
    return WalletResponse.from(wallet, gameName);
  }

  private Map<UUID, String> loadGameNames(List<Wallet> wallets) {
    List<UUID> gameIds = wallets.stream()
      .filter(wallet -> wallet.getType() == WalletType.UGC && wallet.getGameId() != null)
      .map(Wallet::getGameId)
      .distinct()
      .collect(Collectors.toList());

    if (gameIds.isEmpty()) {
      return Map.of();
    }

    return gameRepository.findAllById(gameIds).stream()
      .collect(Collectors.toMap(Game::getId, Game::getName));
  }
}
