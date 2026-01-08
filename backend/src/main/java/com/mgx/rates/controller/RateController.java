package com.mgx.rates.controller;

import com.mgx.auth.security.JwtUserPrincipal;
import com.mgx.common.dto.RejectRequest;
import com.mgx.common.util.ValidationUtil;
import com.mgx.rates.dto.CreateMgcUgcRateRequest;
import com.mgx.rates.dto.CreatePointsMgcRateRequest;
import com.mgx.rates.dto.MgcUgcRateResponse;
import com.mgx.rates.dto.PointsMgcRateResponse;
import com.mgx.rates.model.RateMgcUgc;
import com.mgx.rates.model.RatePointsMgc;
import com.mgx.rates.model.RateStatus;
import com.mgx.rates.service.RateService;
import java.util.List;
import java.util.UUID;
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
@RequestMapping("/v1/admin/rates")
public class RateController {
  private final RateService rateService;

  public RateController(RateService rateService) {
    this.rateService = rateService;
  }

  @PostMapping("/points-mgc")
  @PreAuthorize("hasRole('ADMIN')")
  public PointsMgcRateResponse createPointsMgcRate(
    @AuthenticationPrincipal JwtUserPrincipal principal,
    @RequestBody CreatePointsMgcRateRequest request
  ) {
    RatePointsMgc rate = rateService.createPointsMgcRate(
      request.getPointsPerMgc(),
      request.getActiveFrom(),
      principal.getUserId()
    );
    return PointsMgcRateResponse.from(rate);
  }

  @PostMapping("/mgc-ugc")
  @PreAuthorize("hasRole('ADMIN')")
  public MgcUgcRateResponse createMgcUgcRate(
    @AuthenticationPrincipal JwtUserPrincipal principal,
    @RequestBody CreateMgcUgcRateRequest request
  ) {
    RateMgcUgc rate = rateService.createMgcUgcRate(
      request.getGameId(),
      request.getUgcPerMgc(),
      request.getActiveFrom(),
      principal.getUserId()
    );
    return MgcUgcRateResponse.from(rate);
  }

  @GetMapping("/mgc-ugc")
  @PreAuthorize("hasRole('ADMIN')")
  public List<MgcUgcRateResponse> listMgcUgcRates(
    @RequestParam(required = false) RateStatus status
  ) {
    return rateService.listMgcUgcRates(status).stream()
      .map(MgcUgcRateResponse::from)
      .toList();
  }

  @PostMapping("/mgc-ugc/{rateId}/approve")
  @PreAuthorize("hasRole('ADMIN')")
  public MgcUgcRateResponse approveMgcUgcRate(
    @AuthenticationPrincipal JwtUserPrincipal principal,
    @PathVariable UUID rateId
  ) {
    RateMgcUgc rate = rateService.approveMgcUgcRate(rateId, principal.getUserId());
    return MgcUgcRateResponse.from(rate);
  }

  @PostMapping("/mgc-ugc/{rateId}/reject")
  @PreAuthorize("hasRole('ADMIN')")
  public MgcUgcRateResponse rejectMgcUgcRate(
    @AuthenticationPrincipal JwtUserPrincipal principal,
    @PathVariable UUID rateId,
    @RequestBody RejectRequest request
  ) {
    ValidationUtil.requireNonBlank(request.getReason(), "reason");
    RateMgcUgc rate = rateService.rejectMgcUgcRate(
      rateId,
      principal.getUserId(),
      request.getReason()
    );
    return MgcUgcRateResponse.from(rate);
  }
}
