package com.mgx.bank.controller;

import com.mgx.auth.security.JwtUserPrincipal;
import com.mgx.bank.client.BankPointsClient;
import com.mgx.bank.dto.PointsBalanceResponse;
import com.mgx.common.exception.WalletNotFoundException;
import com.mgx.wallet.model.WalletType;
import com.mgx.wallet.service.WalletService;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/points")
public class PointsController {
  private static final BigDecimal DEFAULT_POINTS = new BigDecimal("1000000");

  private final BankPointsClient bankPointsClient;
  private final WalletService walletService;

  public PointsController(BankPointsClient bankPointsClient, WalletService walletService) {
    this.bankPointsClient = bankPointsClient;
    this.walletService = walletService;
  }

  @GetMapping("/balance")
  @PreAuthorize("isAuthenticated()")
  public PointsBalanceResponse getBalance(@AuthenticationPrincipal JwtUserPrincipal principal) {
    UUID userId = principal.getUserId();
    BigDecimal bankPoints = DEFAULT_POINTS;
    PointsBalanceResponse response = bankPointsClient.getPoints(userId);
    if (response != null && response.getPointsAvailable() != null) {
      bankPoints = response.getPointsAvailable();
    }

    BigDecimal walletPoints = null;
    try {
      walletPoints = walletService.getWalletByUserAndType(userId, WalletType.REWARD_POINTS, null).getBalance();
    } catch (WalletNotFoundException ex) {
      walletPoints = null;
    }

    if (walletPoints != null && bankPoints.compareTo(walletPoints) > 0) {
      BigDecimal delta = bankPoints.subtract(walletPoints);
      try {
        PointsBalanceResponse adjusted = bankPointsClient.debitPoints(userId, delta);
        if (adjusted != null && adjusted.getPointsAvailable() != null) {
          bankPoints = adjusted.getPointsAvailable();
        } else {
          bankPoints = walletPoints;
        }
      } catch (Exception ex) {
        bankPoints = walletPoints;
      }
    }

    PointsBalanceResponse result = new PointsBalanceResponse();
    result.setUserId(userId.toString());
    result.setPointsAvailable(bankPoints);
    return result;
  }
}
