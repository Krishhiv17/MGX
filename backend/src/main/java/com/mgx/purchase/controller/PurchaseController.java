package com.mgx.purchase.controller;

import com.mgx.auth.security.JwtUserPrincipal;
import com.mgx.purchase.dto.PurchaseRequest;
import com.mgx.purchase.dto.PurchaseResponse;
import com.mgx.purchase.model.Purchase;
import com.mgx.purchase.service.PurchaseService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/purchases")
public class PurchaseController {
  private final PurchaseService purchaseService;

  public PurchaseController(PurchaseService purchaseService) {
    this.purchaseService = purchaseService;
  }

  @PostMapping
  @PreAuthorize("hasRole('USER')")
  public PurchaseResponse purchase(
    @AuthenticationPrincipal JwtUserPrincipal principal,
    @RequestHeader("Idempotency-Key") String idempotencyKey,
    @RequestBody PurchaseRequest request
  ) {
    Purchase purchase = purchaseService.createPurchase(
      principal.getUserId(),
      request.getGameId(),
      request.getMgcAmount(),
      request.getUgcAmount(),
      idempotencyKey
    );
    return PurchaseResponse.from(purchase);
  }
}
