package com.mgx.topup.controller;

import com.mgx.auth.security.JwtUserPrincipal;
import com.mgx.topup.dto.TopupRequest;
import com.mgx.topup.dto.TopupResponse;
import com.mgx.topup.model.Topup;
import com.mgx.topup.service.TopupService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/topups")
public class TopupController {
  private final TopupService topupService;

  public TopupController(TopupService topupService) {
    this.topupService = topupService;
  }

  @PostMapping
  @PreAuthorize("hasRole('USER')")
  public TopupResponse topup(
    @AuthenticationPrincipal JwtUserPrincipal principal,
    @RequestHeader("Idempotency-Key") String idempotencyKey,
    @RequestBody TopupRequest request
  ) {
    Topup topup = topupService.createTopup(
      principal.getUserId(),
      request.getPointsAmount(),
      request.getMgcAmount(),
      idempotencyKey
    );
    return TopupResponse.from(topup);
  }

  @GetMapping
  @PreAuthorize("hasRole('USER')")
  public List<TopupResponse> listTopups(@AuthenticationPrincipal JwtUserPrincipal principal) {
    return topupService.listTopups(principal.getUserId()).stream()
      .map(TopupResponse::from)
      .collect(Collectors.toList());
  }
}
