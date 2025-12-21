package com.mgx.rates.repository;

import com.mgx.rates.model.RatePointsMgc;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RatePointsMgcRepository extends JpaRepository<RatePointsMgc, UUID> {
  @Query("SELECT r FROM RatePointsMgc r WHERE r.activeFrom <= :now AND (r.activeTo IS NULL OR r.activeTo > :now)")
  Optional<RatePointsMgc> findActiveRate(@Param("now") OffsetDateTime now);
}
