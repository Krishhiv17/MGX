package com.mgx.fx.repository;

import com.mgx.fx.model.FxRate;
import com.mgx.fx.model.FxRateId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FxRateRepository extends JpaRepository<FxRate, FxRateId> {
  List<FxRate> findByWindowId(UUID windowId);
}
