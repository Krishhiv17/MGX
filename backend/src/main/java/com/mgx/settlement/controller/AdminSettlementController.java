package com.mgx.settlement.controller;

import com.mgx.common.dto.RejectRequest;
import com.mgx.settlement.dto.SettlementResponse;
import com.mgx.settlement.model.SettlementBatch;
import com.mgx.settlement.model.SettlementBatchStatus;
import com.mgx.settlement.repository.SettlementBatchRepository;
import com.mgx.settlement.service.SettlementService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/settlements")
public class AdminSettlementController {
  private final SettlementService settlementService;
  private final SettlementBatchRepository batchRepository;

  public AdminSettlementController(
    SettlementService settlementService,
    SettlementBatchRepository batchRepository
  ) {
    this.settlementService = settlementService;
    this.batchRepository = batchRepository;
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public List<SettlementResponse> listBatches(
    @RequestParam(required = false) SettlementBatchStatus status
  ) {
    List<SettlementBatch> batches = status == null
      ? batchRepository.findAllByOrderByRequestedAtDesc()
      : batchRepository.findByStatusOrderByRequestedAtDesc(status);
    return batches.stream().map(SettlementResponse::from).collect(Collectors.toList());
  }

  @PostMapping("/{batchId}/approve")
  @PreAuthorize("hasRole('ADMIN')")
  public SettlementResponse approveBatch(@PathVariable java.util.UUID batchId) {
    return SettlementResponse.from(settlementService.approveSettlement(batchId));
  }

  @PostMapping("/{batchId}/reject")
  @PreAuthorize("hasRole('ADMIN')")
  public SettlementResponse rejectBatch(
    @PathVariable java.util.UUID batchId,
    @RequestBody RejectRequest request
  ) {
    return SettlementResponse.from(settlementService.rejectSettlement(batchId, request.getReason()));
  }
}
