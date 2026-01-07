package com.mgx.game.service;

import com.mgx.game.model.GameAllowedCountry;
import com.mgx.game.repository.GameAllowedCountryRepository;
import com.mgx.supportedcountry.model.SupportedCountry;
import com.mgx.supportedcountry.repository.SupportedCountryRepository;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class GameCountryService {
  private final GameAllowedCountryRepository gameAllowedCountryRepository;
  private final SupportedCountryRepository supportedCountryRepository;

  public GameCountryService(
    GameAllowedCountryRepository gameAllowedCountryRepository,
    SupportedCountryRepository supportedCountryRepository
  ) {
    this.gameAllowedCountryRepository = gameAllowedCountryRepository;
    this.supportedCountryRepository = supportedCountryRepository;
  }

  public List<String> normalizeAndValidate(List<String> allowedCountries) {
    if (allowedCountries == null || allowedCountries.isEmpty()) {
      throw new IllegalArgumentException("allowedCountries is required");
    }

    LinkedHashSet<String> normalized = allowedCountries.stream()
      .filter(code -> code != null && !code.isBlank())
      .map(code -> code.trim().toUpperCase(Locale.ROOT))
      .collect(Collectors.toCollection(LinkedHashSet::new));

    if (normalized.isEmpty()) {
      throw new IllegalArgumentException("allowedCountries is required");
    }

    List<SupportedCountry> countries = supportedCountryRepository.findAllById(normalized);
    Map<String, SupportedCountry> byCode = countries.stream()
      .collect(Collectors.toMap(SupportedCountry::getCountryCode, country -> country));

    for (String code : normalized) {
      SupportedCountry country = byCode.get(code);
      if (country == null || !"ACTIVE".equalsIgnoreCase(country.getStatus())) {
        throw new IllegalArgumentException("Country is not supported: " + code);
      }
    }

    return new ArrayList<>(normalized);
  }

  public void setAllowedCountries(UUID gameId, List<String> allowedCountries) {
    gameAllowedCountryRepository.deleteByGameId(gameId);
    List<GameAllowedCountry> rows = allowedCountries.stream()
      .map(code -> {
        GameAllowedCountry row = new GameAllowedCountry();
        row.setGameId(gameId);
        row.setCountryCode(code);
        return row;
      })
      .collect(Collectors.toList());
    gameAllowedCountryRepository.saveAll(rows);
  }

  public boolean isAllowed(UUID gameId, String countryCode) {
    return gameAllowedCountryRepository.existsByGameIdAndCountryCode(gameId, countryCode);
  }

  public List<String> getAllowedCountries(UUID gameId) {
    return gameAllowedCountryRepository.findByGameId(gameId).stream()
      .map(GameAllowedCountry::getCountryCode)
      .sorted()
      .collect(Collectors.toList());
  }

  public Map<UUID, List<String>> getAllowedCountriesForGames(List<UUID> gameIds) {
    if (gameIds.isEmpty()) {
      return Map.of();
    }
    return gameAllowedCountryRepository.findByGameIdIn(gameIds).stream()
      .collect(Collectors.groupingBy(
        GameAllowedCountry::getGameId,
        Collectors.mapping(GameAllowedCountry::getCountryCode, Collectors.toList())
      ));
  }
}
