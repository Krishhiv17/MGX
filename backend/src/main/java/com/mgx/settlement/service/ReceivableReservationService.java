package com.mgx.settlement.service;

import com.mgx.settlement.model.Receivable;
import com.mgx.settlement.model.ReceivableStatus;
import com.mgx.settlement.repository.ReceivableRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReceivableReservationService {
  private final ReceivableRepository receivableRepository;

  public ReceivableReservationService(ReceivableRepository receivableRepository) {
    this.receivableRepository = receivableRepository;
  }

  @Transactional
  public ReservationResult reserveReceivables(UUID developerId, UUID batchId) {
    List<Receivable> receivables = receivableRepository.findUnsettledByDeveloperIdForUpdate(developerId);
    BigDecimal total = BigDecimal.ZERO;

    for (Receivable receivable : receivables) {
      receivable.setStatus(ReceivableStatus.RESERVED);
      receivable.setReservedAt(OffsetDateTime.now());
      receivable.setSettlementBatchId(batchId);
      total = total.add(receivable.getAmountDue());
    }

    receivableRepository.saveAll(receivables);
    return new ReservationResult(receivables, total);
  }

  public static class ReservationResult {
    private final List<Receivable> receivables;
    private final BigDecimal totalAmount;

    public ReservationResult(List<Receivable> receivables, BigDecimal totalAmount) {
      this.receivables = receivables;
      this.totalAmount = totalAmount;
    }

    public List<Receivable> getReceivables() {
      return receivables;
    }

    public BigDecimal getTotalAmount() {
      return totalAmount;
    }
  }
}
