package com.mgx.fx.scheduler;

import com.mgx.fx.service.FxService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FxRefreshScheduler {
  private final FxService fxService;

  public FxRefreshScheduler(FxService fxService) {
    this.fxService = fxService;
  }

  @Scheduled(cron = "0 0 0,12 * * *")
  public void refreshFxRates() {
    try {
      fxService.refreshFxRates();
    } catch (Exception ex) {
      // Avoid crashing scheduler on failures
    }
  }
}
