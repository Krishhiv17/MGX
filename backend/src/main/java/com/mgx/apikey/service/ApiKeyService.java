package com.mgx.apikey.service;

import com.mgx.apikey.model.ApiKey;
import com.mgx.apikey.model.ApiKeyStatus;
import com.mgx.apikey.repository.ApiKeyRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ApiKeyService {
  private final ApiKeyRepository apiKeyRepository;

  public ApiKeyService(ApiKeyRepository apiKeyRepository) {
    this.apiKeyRepository = apiKeyRepository;
  }

  public ApiKey createKey(String ownerName, List<String> scopes, String rawKey) {
    ApiKey apiKey = new ApiKey();
    apiKey.setOwnerName(ownerName);
    apiKey.setScopes(String.join(",", scopes));
    apiKey.setKeyHash(hash(rawKey));
    apiKey.setStatus(ApiKeyStatus.ACTIVE);
    return apiKeyRepository.save(apiKey);
  }

  public Optional<ApiKey> findActiveByRawKey(String rawKey) {
    return apiKeyRepository.findByKeyHashAndStatus(hash(rawKey), ApiKeyStatus.ACTIVE);
  }

  public String generateRawKey() {
    return UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
  }

  public String hash(String rawKey) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashed = digest.digest(rawKey.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hashed);
    } catch (Exception ex) {
      throw new IllegalStateException("Failed to hash API key");
    }
  }
}
