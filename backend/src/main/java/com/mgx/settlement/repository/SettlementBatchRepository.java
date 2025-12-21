package com.mgx.settlement.repository;

import com.mgx.settlement.model.SettlementBatch;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementBatchRepository extends JpaRepository<SettlementBatch, UUID> {
  List<SettlementBatch> findByDeveloperIdOrderByRequestedAtDesc(UUID developerId);
}
