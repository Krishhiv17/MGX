package com.mgx.settlement.controller;

import com.mgx.settlement.dto.ReceivableResponse;
import com.mgx.settlement.model.ReceivableStatus;
import com.mgx.settlement.repository.ReceivableRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/receivables")
public class AdminReceivableController {
  private final ReceivableRepository receivableRepository;

  public AdminReceivableController(ReceivableRepository receivableRepository) {
    this.receivableRepository = receivableRepository;
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public List<ReceivableResponse> listReceivables(
    @RequestParam(required = false) ReceivableStatus status,
    @RequestParam(required = false) UUID developerId
  ) {
    List<com.mgx.settlement.model.Receivable> receivables;
    if (developerId != null && status != null) {
      receivables = receivableRepository.findByDeveloperIdAndStatusOrderByCreatedAtAsc(developerId, status);
    } else if (developerId != null) {
      receivables = receivableRepository.findAll().stream()
        .filter(r -> r.getDeveloperId().equals(developerId))
        .collect(Collectors.toList());
    } else if (status != null) {
      receivables = receivableRepository.findAll().stream()
        .filter(r -> r.getStatus() == status)
        .collect(Collectors.toList());
    } else {
      receivables = receivableRepository.findAll();
    }
    return receivables.stream().map(ReceivableResponse::from).collect(Collectors.toList());
  }
}
