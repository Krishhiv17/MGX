package com.mgx.developer.controller;

import com.mgx.common.util.ValidationUtil;
import com.mgx.developer.dto.CreateDeveloperRequest;
import com.mgx.developer.dto.DeveloperResponse;
import com.mgx.developer.model.Developer;
import com.mgx.developer.model.DeveloperStatus;
import com.mgx.developer.repository.DeveloperRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
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

  public DeveloperController(DeveloperRepository developerRepository) {
    this.developerRepository = developerRepository;
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public DeveloperResponse createDeveloper(@RequestBody CreateDeveloperRequest request) {
    ValidationUtil.requireCurrency(request.getSettlementCurrency());

    Developer developer = new Developer();
    developer.setName(request.getName());
    developer.setSettlementCurrency(request.getSettlementCurrency());
    developer.setBankAccountRef(request.getBankAccountRef());
    developer.setStatus(request.getStatus() == null ? DeveloperStatus.ACTIVE : request.getStatus());
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
}
