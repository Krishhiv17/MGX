package com.mgx.rates.controller;

import com.mgx.rates.dto.MgcUgcRateResponse;
import com.mgx.rates.model.RateMgcUgc;
import com.mgx.rates.service.RateService;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/rates")
public class PublicRateController {
  private final RateService rateService;

  public PublicRateController(RateService rateService) {
    this.rateService = rateService;
  }

  @GetMapping("/games/{gameId}/mgc-ugc")
  @PreAuthorize("isAuthenticated()")
  public MgcUgcRateResponse getMgcUgcRate(@PathVariable UUID gameId) {
    RateMgcUgc rate = rateService.getActiveMgcToUgcRate(gameId);
    return MgcUgcRateResponse.from(rate);
  }
}
