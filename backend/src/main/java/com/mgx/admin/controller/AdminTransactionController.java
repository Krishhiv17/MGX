package com.mgx.admin.controller;

import com.mgx.admin.dto.AdminTransactionResponse;
import com.mgx.purchase.repository.PurchaseRepository;
import com.mgx.settlement.repository.ReceivableRepository;
import com.mgx.topup.repository.TopupRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/transactions")
public class AdminTransactionController {
  private final TopupRepository topupRepository;
  private final PurchaseRepository purchaseRepository;
  private final ReceivableRepository receivableRepository;

  public AdminTransactionController(
    TopupRepository topupRepository,
    PurchaseRepository purchaseRepository,
    ReceivableRepository receivableRepository
  ) {
    this.topupRepository = topupRepository;
    this.purchaseRepository = purchaseRepository;
    this.receivableRepository = receivableRepository;
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public List<AdminTransactionResponse> listAllTransactions() {
    List<AdminTransactionResponse> results = new ArrayList<>();
    topupRepository.findAll().forEach(topup -> results.add(AdminTransactionResponse.fromTopup(topup)));
    purchaseRepository.findAll().forEach(purchase -> results.add(AdminTransactionResponse.fromPurchase(purchase)));
    receivableRepository.findAll().forEach(receivable -> results.add(AdminTransactionResponse.fromReceivable(receivable)));
    results.sort(Comparator.comparing(AdminTransactionResponse::getCreatedAt).reversed());
    return results;
  }
}
