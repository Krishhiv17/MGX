package com.mgx.developer.repository;

import com.mgx.developer.model.Developer;
import com.mgx.developer.model.DeveloperStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeveloperRepository extends JpaRepository<Developer, UUID> {
  Optional<Developer> findByUserId(UUID userId);

  Optional<Developer> findTopByUserIdOrderByCreatedAtDesc(UUID userId);

  Optional<Developer> findTopByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, DeveloperStatus status);
}
