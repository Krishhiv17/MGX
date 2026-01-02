package com.mgx.rates.repository;

import com.mgx.rates.model.RateMgcUgc;
import com.mgx.rates.model.RateStatus;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RateMgcUgcRepository extends JpaRepository<RateMgcUgc, UUID> {
  @Query("SELECT r FROM RateMgcUgc r WHERE r.gameId = :gameId AND r.status = :status AND r.activeFrom <= :now AND (r.activeTo IS NULL OR r.activeTo > :now)")
  Optional<RateMgcUgc> findActiveRateByGameId(
    @Param("gameId") UUID gameId,
    @Param("now") OffsetDateTime now,
    @Param("status") RateStatus status
  );
}
