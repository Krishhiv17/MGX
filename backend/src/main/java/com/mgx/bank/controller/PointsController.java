package com.mgx.bank.controller;

import com.mgx.auth.security.JwtUserPrincipal;
import com.mgx.bank.client.BankPointsClient;
import com.mgx.bank.dto.PointsBalanceResponse;
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

  public PointsController(BankPointsClient bankPointsClient) {
    this.bankPointsClient = bankPointsClient;
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

    PointsBalanceResponse result = new PointsBalanceResponse();
    result.setUserId(userId.toString());
    result.setPointsAvailable(bankPoints);
    return result;
  }
}
