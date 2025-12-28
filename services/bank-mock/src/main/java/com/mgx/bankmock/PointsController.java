package com.mgx.bankmock;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PointsController {
  private static final BigDecimal DEFAULT_POINTS = new BigDecimal("1000000");
  private static final ConcurrentHashMap<UUID, BigDecimal> balances = new ConcurrentHashMap<>();

  @GetMapping("/points/{userId}")
  public Map<String, Object> getPoints(@PathVariable UUID userId) {
    BigDecimal current = balances.computeIfAbsent(userId, id -> DEFAULT_POINTS);
    return Map.of(
      "userId", userId.toString(),
      "pointsAvailable", current
    );
  }

  @PostMapping("/points/{userId}/debit")
  public ResponseEntity<Map<String, Object>> debitPoints(
    @PathVariable UUID userId,
    @RequestBody Map<String, Object> body
  ) {
    Object amountRaw = body.get("pointsAmount");
    if (amountRaw == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Map.of("message", "pointsAmount is required"));
    }

    BigDecimal amount;
    try {
      amount = new BigDecimal(amountRaw.toString());
    } catch (NumberFormatException ex) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Map.of("message", "pointsAmount must be a number"));
    }

    if (amount.signum() <= 0) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Map.of("message", "pointsAmount must be positive"));
    }

    BigDecimal current = balances.computeIfAbsent(userId, id -> DEFAULT_POINTS);
    if (current.compareTo(amount) < 0) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Map.of("message", "Insufficient balance"));
    }

    BigDecimal updated = current.subtract(amount);
    balances.put(userId, updated);

    return ResponseEntity.ok(
      Map.of(
        "userId", userId.toString(),
        "pointsAvailable", updated
      )
    );
  }
}
