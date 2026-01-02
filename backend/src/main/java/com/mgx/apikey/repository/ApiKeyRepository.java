package com.mgx.apikey.repository;

import com.mgx.apikey.model.ApiKey;
import com.mgx.apikey.model.ApiKeyStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {
  Optional<ApiKey> findByKeyHashAndStatus(String keyHash, ApiKeyStatus status);
}
