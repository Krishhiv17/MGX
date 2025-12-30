package com.mgx.rates.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgx.common.exception.RateNotFoundException;
import com.mgx.common.util.ValidationUtil;
import com.mgx.rates.model.RateMgcUgc;
import com.mgx.rates.model.RatePointsMgc;
import com.mgx.rates.repository.RateMgcUgcRepository;
import com.mgx.rates.repository.RatePointsMgcRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RateService {
  private static final Duration DEFAULT_TTL = Duration.ofHours(1);
  private static final String POINTS_MGC_KEY = "rate:points_mgc:active";
  private static final String MGC_UGC_KEY_PREFIX = "rate:mgc_ugc:";

  private final RatePointsMgcRepository pointsMgcRepository;
  private final RateMgcUgcRepository mgcUgcRepository;
  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;
  private final BigDecimal defaultUgcPerMgc;

  public RateService(
    RatePointsMgcRepository pointsMgcRepository,
    RateMgcUgcRepository mgcUgcRepository,
    StringRedisTemplate redisTemplate,
    ObjectMapper objectMapper,
    @Value("${mgx.rates.default-ugc-per-mgc:0}") BigDecimal defaultUgcPerMgc
  ) {
    this.pointsMgcRepository = pointsMgcRepository;
    this.mgcUgcRepository = mgcUgcRepository;
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
    this.defaultUgcPerMgc = defaultUgcPerMgc == null ? BigDecimal.ZERO : defaultUgcPerMgc;
  }

  public RatePointsMgc getActivePointsToMgcRate() {
    Optional<RatePointsMgc> cached = getCachedRate(POINTS_MGC_KEY, RatePointsMgc.class);
    if (cached.isPresent()) {
      return cached.get();
    }

    OffsetDateTime now = OffsetDateTime.now();
    RatePointsMgc rate = pointsMgcRepository.findActiveRate(now)
      .orElseThrow(() -> new RateNotFoundException("Active points to MGC rate not found"));
    cacheRate(POINTS_MGC_KEY, rate, ttlFor(rate.getActiveTo()));
    return rate;
  }

  public RateMgcUgc getActiveMgcToUgcRate(UUID gameId) {
    String key = mgcUgcKey(gameId);
    Optional<RateMgcUgc> cached = getCachedRate(key, RateMgcUgc.class);
    if (cached.isPresent()) {
      return cached.get();
    }

    OffsetDateTime now = OffsetDateTime.now();
    RateMgcUgc rate = mgcUgcRepository.findActiveRateByGameId(gameId, now).orElse(null);
    if (rate == null) {
      if (defaultUgcPerMgc.signum() <= 0) {
        throw new RateNotFoundException("Active MGC to UGC rate not found");
      }
      rate = createMgcUgcRate(gameId, defaultUgcPerMgc, now, null);
    }
    cacheRate(key, rate, ttlFor(rate.getActiveTo()));
    return rate;
  }

  @Transactional
  public RatePointsMgc createPointsMgcRate(
    BigDecimal pointsPerMgc,
    OffsetDateTime activeFrom,
    UUID createdBy
  ) {
    ValidationUtil.requirePositive(pointsPerMgc, "pointsPerMgc");
    OffsetDateTime now = OffsetDateTime.now();

    pointsMgcRepository.findActiveRate(now).ifPresent(existing -> {
      existing.setActiveTo(now);
      pointsMgcRepository.save(existing);
    });

    RatePointsMgc rate = new RatePointsMgc();
    rate.setPointsPerMgc(pointsPerMgc);
    rate.setActiveFrom(activeFrom == null ? now : activeFrom);
    rate.setActiveTo(null);
    rate.setCreatedBy(createdBy);

    RatePointsMgc saved = pointsMgcRepository.save(rate);
    redisTemplate.delete(POINTS_MGC_KEY);
    return saved;
  }

  @Transactional
  public RateMgcUgc createMgcUgcRate(
    UUID gameId,
    BigDecimal ugcPerMgc,
    OffsetDateTime activeFrom,
    UUID createdBy
  ) {
    ValidationUtil.requirePositive(ugcPerMgc, "ugcPerMgc");
    OffsetDateTime now = OffsetDateTime.now();

    mgcUgcRepository.findActiveRateByGameId(gameId, now).ifPresent(existing -> {
      existing.setActiveTo(now);
      mgcUgcRepository.save(existing);
    });

    RateMgcUgc rate = new RateMgcUgc();
    rate.setGameId(gameId);
    rate.setUgcPerMgc(ugcPerMgc);
    rate.setActiveFrom(activeFrom == null ? now : activeFrom);
    rate.setActiveTo(null);
    rate.setCreatedBy(createdBy);

    RateMgcUgc saved = mgcUgcRepository.save(rate);
    redisTemplate.delete(mgcUgcKey(gameId));
    return saved;
  }

  private Duration ttlFor(OffsetDateTime activeTo) {
    if (activeTo == null) {
      return DEFAULT_TTL;
    }
    Duration remaining = Duration.between(OffsetDateTime.now(), activeTo);
    if (remaining.isNegative() || remaining.isZero()) {
      return Duration.ZERO;
    }
    return remaining.compareTo(DEFAULT_TTL) < 0 ? remaining : DEFAULT_TTL;
  }

  private String mgcUgcKey(UUID gameId) {
    return MGC_UGC_KEY_PREFIX + gameId + ":active";
  }

  private <T> Optional<T> getCachedRate(String key, Class<T> type) {
    String cached = redisTemplate.opsForValue().get(key);
    if (cached == null || cached.isEmpty()) {
      return Optional.empty();
    }
    try {
      return Optional.of(objectMapper.readValue(cached, type));
    } catch (Exception ex) {
      redisTemplate.delete(key);
      return Optional.empty();
    }
  }

  private void cacheRate(String key, Object rate, Duration ttl) {
    if (ttl.isZero()) {
      return;
    }
    try {
      String json = objectMapper.writeValueAsString(rate);
      redisTemplate.opsForValue().set(key, json, ttl);
    } catch (JsonProcessingException ex) {
      // Skip cache on serialization errors
    }
  }
}
