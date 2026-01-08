package com.mgx.developer.controller;

import com.mgx.auth.security.JwtUserPrincipal;
import com.mgx.common.dto.RejectRequest;
import com.mgx.common.util.ValidationUtil;
import com.mgx.developer.dto.CreateDeveloperRequest;
import com.mgx.developer.dto.DeveloperResponse;
import com.mgx.developer.dto.DeveloperSummaryResponse;
import com.mgx.developer.model.Developer;
import com.mgx.developer.model.DeveloperStatus;
import com.mgx.developer.repository.DeveloperRepository;
import com.mgx.settlement.model.ReceivableStatus;
import com.mgx.settlement.repository.ReceivableRepository;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/developers")
public class DeveloperController {
  private final DeveloperRepository developerRepository;
  private final ReceivableRepository receivableRepository;

  public DeveloperController(
    DeveloperRepository developerRepository,
    ReceivableRepository receivableRepository
  ) {
    this.developerRepository = developerRepository;
    this.receivableRepository = receivableRepository;
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public DeveloperResponse createDeveloper(@RequestBody CreateDeveloperRequest request) {
    ValidationUtil.requireCurrency(request.getSettlementCurrency());

    Developer developer = new Developer();
    developer.setName(request.getName());
    developer.setSettlementCurrency(request.getSettlementCurrency());
    developer.setBankAccountRef(request.getBankAccountRef());
    developer.setStatus(request.getStatus() == null ? DeveloperStatus.PENDING_APPROVAL : request.getStatus());
    if (request.getUserId() != null && !request.getUserId().isBlank()) {
      developer.setUserId(UUID.fromString(request.getUserId()));
    }

    return DeveloperResponse.from(developerRepository.save(developer));
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public List<DeveloperResponse> listDevelopers() {
    return developerRepository.findAll().stream().map(DeveloperResponse::from).collect(Collectors.toList());
  }

  @GetMapping("/{developerId}")
  @PreAuthorize("hasRole('ADMIN')")
  public DeveloperResponse getDeveloper(@PathVariable UUID developerId) {
    Developer developer = developerRepository.findById(developerId)
      .orElseThrow(() -> new IllegalArgumentException("Developer not found"));
    return DeveloperResponse.from(developer);
  }

  @PostMapping("/{developerId}/approve")
  @PreAuthorize("hasRole('ADMIN')")
  public DeveloperResponse approveDeveloper(
    @AuthenticationPrincipal JwtUserPrincipal principal,
    @PathVariable UUID developerId
  ) {
    Developer developer = developerRepository.findById(developerId)
      .orElseThrow(() -> new IllegalArgumentException("Developer not found"));
    developer.setStatus(DeveloperStatus.ACTIVE);
    developer.setApprovedBy(principal.getUserId());
    developer.setApprovedAt(java.time.OffsetDateTime.now());
    developer.setRejectionReason(null);
    return DeveloperResponse.from(developerRepository.save(developer));
  }

  @PostMapping("/{developerId}/reject")
  @PreAuthorize("hasRole('ADMIN')")
  public DeveloperResponse rejectDeveloper(
    @AuthenticationPrincipal JwtUserPrincipal principal,
    @PathVariable UUID developerId,
    @RequestBody RejectRequest request
  ) {
    ValidationUtil.requireNonBlank(request.getReason(), "reason");
    Developer developer = developerRepository.findById(developerId)
      .orElseThrow(() -> new IllegalArgumentException("Developer not found"));
    developer.setStatus(DeveloperStatus.REJECTED);
    developer.setApprovedBy(principal.getUserId());
    developer.setApprovedAt(java.time.OffsetDateTime.now());
    developer.setRejectionReason(request.getReason());
    return DeveloperResponse.from(developerRepository.save(developer));
  }

  @GetMapping("/summary")
  @PreAuthorize("hasRole('ADMIN')")
  public List<DeveloperSummaryResponse> listDeveloperSummaries() {
    List<Developer> developers = developerRepository.findAll();
    java.util.Map<UUID, java.math.BigDecimal> totals = receivableRepository
      .sumAmountsByDeveloperAndStatus(ReceivableStatus.UNSETTLED)
      .stream()
      .collect(Collectors.toMap(
        ReceivableRepository.DeveloperTotal::getDeveloperId,
        ReceivableRepository.DeveloperTotal::getTotalAmount
      ));
    return developers.stream()
      .map(dev -> DeveloperSummaryResponse.from(
        dev,
        totals.getOrDefault(dev.getId(), java.math.BigDecimal.ZERO)
      ))
      .collect(Collectors.toList());
  }
}
