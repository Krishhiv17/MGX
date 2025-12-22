package com.mgx.common.service;

import java.time.Duration;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class IdempotencyService {
  private static final Duration DEFAULT_TTL = Duration.ofHours(24);

  private final StringRedisTemplate redisTemplate;

  public IdempotencyService(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  public boolean checkAndStore(String key, String value) {
    Boolean success = redisTemplate.opsForValue().setIfAbsent(key, value, DEFAULT_TTL);
    return Boolean.TRUE.equals(success);
  }

  public Optional<String> getExistingResult(String key) {
    return Optional.ofNullable(redisTemplate.opsForValue().get(key));
  }

  public String buildKey(String operation, String userId, String idempotencyKey) {
    return "idempo:" + operation + ":" + userId + ":" + idempotencyKey;
  }
}
