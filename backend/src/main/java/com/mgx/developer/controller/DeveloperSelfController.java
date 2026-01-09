package com.mgx.developer.controller;

import com.mgx.auth.security.JwtUserPrincipal;
import com.mgx.common.util.ValidationUtil;
import com.mgx.developer.dto.DeveloperProfileRequest;
import com.mgx.developer.dto.DeveloperResponse;
import com.mgx.developer.model.Developer;
import com.mgx.developer.model.DeveloperStatus;
import com.mgx.developer.repository.DeveloperRepository;
import java.util.Optional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/developer/profile")
public class DeveloperSelfController {
  private final DeveloperRepository developerRepository;

  public DeveloperSelfController(DeveloperRepository developerRepository) {
    this.developerRepository = developerRepository;
  }

  @GetMapping
  @PreAuthorize("hasRole('DEVELOPER')")
  public DeveloperResponse getProfile(@AuthenticationPrincipal JwtUserPrincipal principal) {
    Developer developer = developerRepository.findTopByUserIdOrderByCreatedAtDesc(principal.getUserId())
      .orElseThrow(() -> new IllegalArgumentException("Developer profile not found"));
    return DeveloperResponse.from(developer);
  }

  @PostMapping("/request")
  @PreAuthorize("hasRole('DEVELOPER')")
  public DeveloperResponse requestProfile(
    @AuthenticationPrincipal JwtUserPrincipal principal,
    @RequestBody DeveloperProfileRequest request
  ) {
    ValidationUtil.requireNonBlank(request.getName(), "name");
    ValidationUtil.requireCurrency(request.getSettlementCurrency());
    ValidationUtil.requireNonBlank(request.getBankAccountRef(), "bankAccountRef");

    Optional<Developer> existing = developerRepository.findTopByUserIdOrderByCreatedAtDesc(principal.getUserId());
    if (existing.isPresent()) {
      Developer current = existing.get();
      if (current.getStatus() == DeveloperStatus.PENDING_APPROVAL || current.getStatus() == DeveloperStatus.ACTIVE) {
        return DeveloperResponse.from(current);
      }
    }

    Developer developer = new Developer();
    developer.setName(request.getName());
    developer.setSettlementCurrency(request.getSettlementCurrency());
    developer.setBankAccountRef(request.getBankAccountRef());
    developer.setUserId(principal.getUserId());
    developer.setStatus(DeveloperStatus.PENDING_APPROVAL);
    developer.setRejectionReason(null);

    return DeveloperResponse.from(developerRepository.save(developer));
  }
}
