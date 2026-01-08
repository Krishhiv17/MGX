package com.mgx.apikey.controller;

import com.mgx.apikey.dto.ApiKeyCreateRequest;
import com.mgx.apikey.dto.ApiKeyResponse;
import com.mgx.apikey.model.ApiKey;
import com.mgx.apikey.service.ApiKeyService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/api-keys")
public class AdminApiKeyController {
  private final ApiKeyService apiKeyService;

  public AdminApiKeyController(ApiKeyService apiKeyService) {
    this.apiKeyService = apiKeyService;
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ApiKeyResponse create(@RequestBody ApiKeyCreateRequest request) {
    if (request.getOwnerName() == null || request.getOwnerName().isBlank()) {
      throw new IllegalArgumentException("ownerName is required");
    }
    List<String> scopes = request.getScopes() == null ? List.of("private") : request.getScopes();
    if (scopes.isEmpty()) {
      scopes = List.of("private");
    }
    String rawKey = apiKeyService.generateRawKey();
    ApiKey apiKey = apiKeyService.createKey(request.getOwnerName(), scopes, rawKey);
    return ApiKeyResponse.from(apiKey, rawKey);
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public List<ApiKeyResponse> list() {
    return apiKeyService.listKeys().stream()
      .map(key -> ApiKeyResponse.from(key, null))
      .toList();
  }
}
