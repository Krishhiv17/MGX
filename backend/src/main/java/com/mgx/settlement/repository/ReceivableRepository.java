package com.mgx.settlement.repository;

import com.mgx.settlement.model.Receivable;
import com.mgx.settlement.model.ReceivableStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReceivableRepository extends JpaRepository<Receivable, UUID> {
  List<Receivable> findByDeveloperIdAndStatusOrderByCreatedAtAsc(UUID developerId, ReceivableStatus status);

  List<Receivable> findBySettlementBatchId(UUID settlementBatchId);

  @Query(
    value = "SELECT * FROM receivables WHERE developer_id = :developerId AND status = 'UNSETTLED' FOR UPDATE SKIP LOCKED",
    nativeQuery = true
  )
  List<Receivable> findUnsettledByDeveloperIdForUpdate(@Param("developerId") UUID developerId);
}
