package com.mgx.fx.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgx.common.exception.FxWindowNotFoundException;
import com.mgx.common.util.ValidationUtil;
import com.mgx.fx.client.FxLocalClient;
import com.mgx.fx.dto.FxRateResponse;
import com.mgx.fx.model.FxRate;
import com.mgx.fx.model.FxRateWindow;
import com.mgx.fx.model.FxWindowStatus;
import com.mgx.fx.repository.FxRateRepository;
import com.mgx.fx.repository.FxRateWindowRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FxService {
  private static final String WINDOW_CACHE_KEY = "fx:window:current";
  private static final String RATES_CACHE_PREFIX = "fx:rates:";
  private static final int SCALE = 12;

  private final FxLocalClient fxLocalClient;
  private final FxRateWindowRepository windowRepository;
  private final FxRateRepository rateRepository;
  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;
  private final long windowHours;

  public FxService(
    FxLocalClient fxLocalClient,
    FxRateWindowRepository windowRepository,
    FxRateRepository rateRepository,
    StringRedisTemplate redisTemplate,
    ObjectMapper objectMapper,
    @Value("${mgx.fx.window-hours:12}") long windowHours
  ) {
    this.fxLocalClient = fxLocalClient;
    this.windowRepository = windowRepository;
    this.rateRepository = rateRepository;
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
    this.windowHours = windowHours;
  }

  public FxRateWindow getCurrentFxWindow() {
    Optional<FxRateWindow> cached = getCachedWindow();
    if (cached.isPresent()) {
      return cached.get();
    }

    OffsetDateTime now = OffsetDateTime.now();
    Optional<FxRateWindow> existing = windowRepository.findCurrentWindow(now, FxWindowStatus.SUCCESS);
    if (existing.isPresent()) {
      cacheWindow(existing.get());
      return existing.get();
    }

    FxRateWindow refreshed = refreshFxRates();
    if (refreshed.getStatus() != FxWindowStatus.SUCCESS) {
      throw new FxWindowNotFoundException("No active FX window");
    }
    return refreshed;
  }

  public List<FxRate> getFxRatesForWindow(UUID windowId) {
    Optional<List<FxRate>> cached = getCachedRates(windowId);
    if (cached.isPresent()) {
      return cached.get();
    }

    List<FxRate> rates = rateRepository.findByWindowId(windowId);
    cacheRates(windowId, rates);
    return rates;
  }

  public BigDecimal convertCurrency(
    String fromCurrency,
    String toCurrency,
    BigDecimal amount,
    UUID windowId
  ) {
    ValidationUtil.requirePositive(amount, "amount");
    ValidationUtil.requireCurrency(fromCurrency);
    ValidationUtil.requireCurrency(toCurrency);

    Map<String, BigDecimal> rateMap = buildRateMap(windowId);
    BigDecimal amountInUsd = fromCurrency.equals("USD")
      ? amount
      : amount.divide(rateMap.get(fromCurrency), SCALE, RoundingMode.HALF_UP);

    if (toCurrency.equals("USD")) {
      return amountInUsd;
    }

    return amountInUsd.multiply(rateMap.get(toCurrency)).setScale(SCALE, RoundingMode.HALF_UP);
  }

  @Transactional
  public FxRateWindow refreshFxRates() {
    OffsetDateTime now = OffsetDateTime.now();
    try {
      FxRateResponse response = fxLocalClient.fetchRates();
      FxRateWindow window = new FxRateWindow();
      window.setProvider(response.getProvider());
      window.setFetchedAt(response.getFetchedAt());
      window.setValidFrom(now);
      window.setValidTo(now.plusHours(windowHours));
      window.setStatus(FxWindowStatus.SUCCESS);

      FxRateWindow savedWindow = windowRepository.save(window);
      for (Map.Entry<String, BigDecimal> entry : response.getRates().entrySet()) {
        FxRate rate = new FxRate();
        rate.setWindowId(savedWindow.getId());
        rate.setBaseCurrency(response.getBase());
        rate.setQuoteCurrency(entry.getKey());
        rate.setRate(entry.getValue());
        rateRepository.save(rate);
      }

      cacheWindow(savedWindow);
      cacheRates(savedWindow.getId(), rateRepository.findByWindowId(savedWindow.getId()));
      return savedWindow;
    } catch (Exception ex) {
      FxRateWindow failed = new FxRateWindow();
      failed.setProvider("LOCAL_MOCK");
      failed.setFetchedAt(now);
      failed.setValidFrom(now);
      failed.setValidTo(now.plusHours(windowHours));
      failed.setStatus(FxWindowStatus.FAILED);
      return windowRepository.save(failed);
    }
  }

  private Map<String, BigDecimal> buildRateMap(UUID windowId) {
    List<FxRate> rates = getFxRatesForWindow(windowId);
    Map<String, BigDecimal> map = new HashMap<>();
    map.put("USD", BigDecimal.ONE);
    for (FxRate rate : rates) {
      map.put(rate.getQuoteCurrency(), rate.getRate());
    }
    return map;
  }

  private Optional<FxRateWindow> getCachedWindow() {
    String cached = redisTemplate.opsForValue().get(WINDOW_CACHE_KEY);
    if (cached == null || cached.isEmpty()) {
      return Optional.empty();
    }
    try {
      return Optional.of(objectMapper.readValue(cached, FxRateWindow.class));
    } catch (Exception ex) {
      redisTemplate.delete(WINDOW_CACHE_KEY);
      return Optional.empty();
    }
  }

  private void cacheWindow(FxRateWindow window) {
    try {
      String json = objectMapper.writeValueAsString(window);
      redisTemplate.opsForValue().set(WINDOW_CACHE_KEY, json, Duration.ofHours(1));
    } catch (JsonProcessingException ex) {
      // Skip cache on serialization errors
    }
  }

  private Optional<List<FxRate>> getCachedRates(UUID windowId) {
    String key = RATES_CACHE_PREFIX + windowId;
    String cached = redisTemplate.opsForValue().get(key);
    if (cached == null || cached.isEmpty()) {
      return Optional.empty();
    }
    try {
      List<FxRate> rates = objectMapper.readValue(cached, new TypeReference<List<FxRate>>() {});
      return Optional.of(rates);
    } catch (Exception ex) {
      redisTemplate.delete(key);
      return Optional.empty();
    }
  }

  private void cacheRates(UUID windowId, List<FxRate> rates) {
    String key = RATES_CACHE_PREFIX + windowId;
    try {
      String json = objectMapper.writeValueAsString(rates);
      redisTemplate.opsForValue().set(key, json, Duration.ofHours(12));
    } catch (JsonProcessingException ex) {
      // Skip cache on serialization errors
    }
  }
}
