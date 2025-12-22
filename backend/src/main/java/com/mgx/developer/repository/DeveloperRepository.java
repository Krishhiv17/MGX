package com.mgx.developer.repository;

import com.mgx.developer.model.Developer;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeveloperRepository extends JpaRepository<Developer, UUID> {
  Optional<Developer> findByUserId(UUID userId);
}
