package com.mgx.bankmock;

import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PayoutController {

  @PostMapping("/payouts")
  public Map<String, Object> payout(@RequestBody Map<String, Object> request) {
    return Map.of(
      "status", "SUCCESS",
      "payoutId", UUID.randomUUID().toString(),
      "received", request
    );
  }
}
