package com.mgx.apikey.dto;

import com.mgx.apikey.model.ApiKey;
import com.mgx.apikey.model.ApiKeyStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public class ApiKeyResponse {
  private UUID id;
  private String ownerName;
  private String scopes;
  private ApiKeyStatus status;
  private OffsetDateTime createdAt;
  private String key;

  public static ApiKeyResponse from(ApiKey apiKey, String key) {
    ApiKeyResponse response = new ApiKeyResponse();
    response.setId(apiKey.getId());
    response.setOwnerName(apiKey.getOwnerName());
    response.setScopes(apiKey.getScopes());
    response.setStatus(apiKey.getStatus());
    response.setCreatedAt(apiKey.getCreatedAt());
    response.setKey(key);
    return response;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getOwnerName() {
    return ownerName;
  }

  public void setOwnerName(String ownerName) {
    this.ownerName = ownerName;
  }

  public String getScopes() {
    return scopes;
  }

  public void setScopes(String scopes) {
    this.scopes = scopes;
  }

  public ApiKeyStatus getStatus() {
    return status;
  }

  public void setStatus(ApiKeyStatus status) {
    this.status = status;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }
}
