package com.mgx.settlement.controller;

import com.mgx.auth.security.JwtUserPrincipal;
import com.mgx.settlement.dto.ReceivableResponse;
import com.mgx.settlement.dto.SettlementBatchResponse;
import com.mgx.settlement.dto.SettlementRequest;
import com.mgx.settlement.dto.SettlementResponse;
import com.mgx.settlement.model.Receivable;
import com.mgx.settlement.model.ReceivableStatus;
import com.mgx.settlement.model.SettlementBatch;
import com.mgx.settlement.repository.ReceivableRepository;
import com.mgx.settlement.repository.SettlementBatchRepository;
import com.mgx.settlement.service.SettlementService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/developer")
public class SettlementController {
  private final SettlementService settlementService;
  private final ReceivableRepository receivableRepository;
  private final SettlementBatchRepository batchRepository;

  public SettlementController(
    SettlementService settlementService,
    ReceivableRepository receivableRepository,
    SettlementBatchRepository batchRepository
  ) {
    this.settlementService = settlementService;
    this.receivableRepository = receivableRepository;
    this.batchRepository = batchRepository;
  }

  @PostMapping("/settlements/request")
  @PreAuthorize("hasRole('DEVELOPER')")
  public SettlementResponse requestSettlement(
    @AuthenticationPrincipal JwtUserPrincipal principal,
    @RequestBody SettlementRequest request
  ) {
    UUID developerId = request.getDeveloperId();
    if (developerId == null) {
      developerId = settlementService.resolveDeveloperId(principal.getUserId());
    }
    SettlementBatch batch = settlementService.requestSettlement(developerId, principal.getUserId());
    return SettlementResponse.from(batch);
  }

  @GetMapping("/receivables")
  @PreAuthorize("hasRole('DEVELOPER')")
  public List<ReceivableResponse> listReceivables(
    @RequestParam(required = false) UUID developerId,
    @RequestParam(defaultValue = "UNSETTLED") ReceivableStatus status
  ) {
    if (developerId == null) {
      throw new IllegalArgumentException("developerId is required");
    }
    List<Receivable> receivables = receivableRepository
      .findByDeveloperIdAndStatusOrderByCreatedAtAsc(developerId, status);
    return receivables.stream().map(ReceivableResponse::from).collect(Collectors.toList());
  }

  @GetMapping("/settlements/{batchId}")
  @PreAuthorize("hasRole('DEVELOPER')")
  public SettlementBatchResponse getBatch(@PathVariable UUID batchId) {
    SettlementBatch batch = batchRepository.findById(batchId).orElseThrow();
    List<ReceivableResponse> receivables = receivableRepository.findBySettlementBatchId(batchId)
      .stream()
      .map(ReceivableResponse::from)
      .collect(Collectors.toList());
    return SettlementBatchResponse.from(batch, receivables);
  }

  @GetMapping("/settlements")
  @PreAuthorize("hasRole('DEVELOPER')")
  public List<SettlementResponse> listBatches(@RequestParam(required = false) UUID developerId) {
    if (developerId == null) {
      throw new IllegalArgumentException("developerId is required");
    }
    return batchRepository.findByDeveloperIdOrderByRequestedAtDesc(developerId)
      .stream()
      .map(SettlementResponse::from)
      .collect(Collectors.toList());
  }
}
