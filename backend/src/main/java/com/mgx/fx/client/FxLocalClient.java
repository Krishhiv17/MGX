package com.mgx.fx.client;

import com.mgx.fx.dto.FxRateResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class FxLocalClient {
  private final RestTemplate restTemplate;
  private final String fxUrl;

  public FxLocalClient(
    RestTemplateBuilder restTemplateBuilder,
    @Value("${mgx.fx.local-url}") String fxUrl
  ) {
    this.restTemplate = restTemplateBuilder.build();
    this.fxUrl = fxUrl;
  }

  public FxRateResponse fetchRates() {
    return restTemplate.getForObject(fxUrl, FxRateResponse.class);
  }
}
