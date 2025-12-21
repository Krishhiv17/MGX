package com.mgx.settlement.repository;

import com.mgx.settlement.model.Receivable;
import com.mgx.settlement.model.ReceivableStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceivableRepository extends JpaRepository<Receivable, UUID> {
  List<Receivable> findByDeveloperIdAndStatusOrderByCreatedAtAsc(UUID developerId, ReceivableStatus status);

  List<Receivable> findBySettlementBatchId(UUID settlementBatchId);
}
