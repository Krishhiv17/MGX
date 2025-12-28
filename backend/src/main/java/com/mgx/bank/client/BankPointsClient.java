package com.mgx.bank.client;

import com.mgx.bank.dto.PointsBalanceResponse;
import com.mgx.bank.dto.PointsDebitRequest;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class BankPointsClient {
  private final RestTemplate restTemplate;
  private final String baseUrl;

  public BankPointsClient(
    RestTemplateBuilder restTemplateBuilder,
    @Value("${mgx.bank.base-url}") String baseUrl
  ) {
    this.restTemplate = restTemplateBuilder.build();
    this.baseUrl = baseUrl;
  }

  public PointsBalanceResponse getPoints(UUID userId) {
    return restTemplate.getForObject(baseUrl + "/points/" + userId, PointsBalanceResponse.class);
  }

  public PointsBalanceResponse debitPoints(UUID userId, BigDecimal pointsAmount) {
    PointsDebitRequest request = new PointsDebitRequest(pointsAmount);
    return restTemplate.postForObject(
      baseUrl + "/points/" + userId + "/debit",
      request,
      PointsBalanceResponse.class
    );
  }
}
