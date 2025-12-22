package com.mgx.wallet.controller;

import com.mgx.auth.security.JwtUserPrincipal;
import com.mgx.wallet.dto.WalletResponse;
import com.mgx.wallet.model.Wallet;
import com.mgx.wallet.service.WalletService;
import java.util.List;
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

  public WalletController(WalletService walletService) {
    this.walletService = walletService;
  }

  @GetMapping
  @PreAuthorize("hasRole('USER')")
  public List<WalletResponse> getWallets(@AuthenticationPrincipal JwtUserPrincipal principal) {
    List<Wallet> wallets = walletService.getWalletsByUserId(principal.getUserId());
    return wallets.stream().map(WalletResponse::from).collect(Collectors.toList());
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
    return WalletResponse.from(wallet);
  }
}
