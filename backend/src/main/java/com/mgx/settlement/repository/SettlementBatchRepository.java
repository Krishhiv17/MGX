package com.mgx.settlement.repository;

import com.mgx.settlement.model.SettlementBatch;
import com.mgx.settlement.model.SettlementBatchStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementBatchRepository extends JpaRepository<SettlementBatch, UUID> {
  List<SettlementBatch> findByDeveloperIdOrderByRequestedAtDesc(UUID developerId);

  boolean existsByDeveloperIdAndStatusIn(UUID developerId, List<SettlementBatchStatus> statuses);

  List<SettlementBatch> findByStatusOrderByRequestedAtDesc(SettlementBatchStatus status);

  List<SettlementBatch> findAllByOrderByRequestedAtDesc();
}
