package com.mgx.fxmock;

import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FxController {

  @GetMapping("/fx/rates")
  public Map<String, Object> getRates() {
    return Map.of(
      "provider", "LOCAL_MOCK",
      "fetchedAt", Instant.now().toString(),
      "base", "USD",
      "rates", Map.of(
        "USD", 1.0,
        "EUR", 0.92,
        "GBP", 0.79,
        "INR", 83.1,
        "JPY", 155.0
      )
    );
  }
}
