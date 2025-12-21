package com.mgx.fx.repository;

import com.mgx.fx.model.FxRateWindow;
import com.mgx.fx.model.FxWindowStatus;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FxRateWindowRepository extends JpaRepository<FxRateWindow, UUID> {
  @Query("SELECT w FROM FxRateWindow w WHERE w.validFrom <= :now AND w.validTo > :now AND w.status = :status")
  Optional<FxRateWindow> findCurrentWindow(
    @Param("now") OffsetDateTime now,
    @Param("status") FxWindowStatus status
  );
}
