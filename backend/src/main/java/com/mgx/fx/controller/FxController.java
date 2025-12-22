package com.mgx.fx.controller;

import com.mgx.fx.dto.FxCurrentResponse;
import com.mgx.fx.dto.FxWindowResponse;
import com.mgx.fx.model.FxRate;
import com.mgx.fx.model.FxRateWindow;
import com.mgx.fx.repository.FxRateWindowRepository;
import com.mgx.fx.service.FxService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/fx")
public class FxController {
  private final FxService fxService;
  private final FxRateWindowRepository windowRepository;

  public FxController(FxService fxService, FxRateWindowRepository windowRepository) {
    this.fxService = fxService;
    this.windowRepository = windowRepository;
  }

  @PostMapping("/refresh")
  @PreAuthorize("hasRole('ADMIN')")
  public FxWindowResponse refresh() {
    return FxWindowResponse.from(fxService.refreshFxRates());
  }

  @GetMapping("/windows")
  @PreAuthorize("hasRole('ADMIN')")
  public List<FxWindowResponse> listWindows() {
    return windowRepository.findAll(Sort.by(Sort.Direction.DESC, "fetchedAt")).stream()
      .map(FxWindowResponse::from)
      .collect(Collectors.toList());
  }

  @GetMapping("/current")
  @PreAuthorize("hasRole('ADMIN')")
  public FxCurrentResponse current() {
    FxRateWindow window = fxService.getCurrentFxWindow();
    List<FxRate> rates = fxService.getFxRatesForWindow(window.getId());
    Map<String, BigDecimal> rateMap = rates.stream()
      .collect(Collectors.toMap(FxRate::getQuoteCurrency, FxRate::getRate));
    String base = rates.isEmpty() ? "USD" : rates.get(0).getBaseCurrency();
    return new FxCurrentResponse(FxWindowResponse.from(window), base, rateMap);
  }
}
