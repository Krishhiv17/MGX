package com.mgx.topup.repository;

import com.mgx.topup.model.Topup;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopupRepository extends JpaRepository<Topup, UUID> {
  Optional<Topup> findByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey);

  List<Topup> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
