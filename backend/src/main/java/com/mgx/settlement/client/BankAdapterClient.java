package com.mgx.settlement.client;

import com.mgx.settlement.dto.PayoutRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class BankAdapterClient {
  private final RestTemplate restTemplate;
  private final String bankUrl;

  public BankAdapterClient(
    RestTemplateBuilder restTemplateBuilder,
    @Value("${mgx.bank.local-url}") String bankUrl
  ) {
    this.restTemplate = restTemplateBuilder.build();
    this.bankUrl = bankUrl;
  }

  public void sendPayout(PayoutRequest request) {
    restTemplate.postForObject(bankUrl, request, String.class);
  }
}
