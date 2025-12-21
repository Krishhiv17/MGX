package com.mgx.purchase.repository;

import com.mgx.purchase.model.Purchase;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, UUID> {
  Optional<Purchase> findByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey);
}
