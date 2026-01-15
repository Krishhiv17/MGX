package com.mgx.settlement.service;

import com.mgx.common.exception.ReceivableNotFoundException;
import com.mgx.developer.model.Developer;
import com.mgx.developer.repository.DeveloperRepository;
import com.mgx.settlement.client.BankAdapterClient;
import com.mgx.settlement.dto.PayoutRequest;
import com.mgx.settlement.model.Receivable;
import com.mgx.settlement.model.ReceivableStatus;
import com.mgx.settlement.model.SettlementBatch;
import com.mgx.settlement.model.SettlementBatchStatus;
import com.mgx.settlement.repository.ReceivableRepository;
import com.mgx.settlement.repository.SettlementBatchRepository;
import com.mgx.config.RabbitMQConfig;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettlementService {
  private final SettlementBatchRepository batchRepository;
  private final ReceivableRepository receivableRepository;
  private final ReceivableReservationService reservationService;
  private final DeveloperRepository developerRepository;
  private final BankAdapterClient bankAdapterClient;
  private final RabbitTemplate rabbitTemplate;

  public SettlementService(
    SettlementBatchRepository batchRepository,
    ReceivableRepository receivableRepository,
    ReceivableReservationService reservationService,
    DeveloperRepository developerRepository,
    BankAdapterClient bankAdapterClient,
    RabbitTemplate rabbitTemplate
  ) {
    this.batchRepository = batchRepository;
    this.receivableRepository = receivableRepository;
    this.reservationService = reservationService;
    this.developerRepository = developerRepository;
    this.bankAdapterClient = bankAdapterClient;
    this.rabbitTemplate = rabbitTemplate;
  }

  public UUID resolveDeveloperId(UUID userId) {
    return developerRepository
      .findTopByUserIdAndStatusOrderByCreatedAtDesc(
        userId,
        com.mgx.developer.model.DeveloperStatus.ACTIVE
      )
      .map(Developer::getId)
      .orElseThrow(() -> new ReceivableNotFoundException("Developer not linked to user"));
  }

  @Transactional
  public SettlementBatch requestSettlement(UUID developerId, UUID requestedBy) {
    Developer developer = developerRepository.findById(developerId)
      .orElseThrow(() -> new ReceivableNotFoundException("Developer not found"));
    if (developer.getStatus() != com.mgx.developer.model.DeveloperStatus.ACTIVE) {
      throw new ReceivableNotFoundException("Developer is not active");
    }
    if (batchRepository.existsByDeveloperIdAndStatusIn(
      developerId,
      List.of(SettlementBatchStatus.REQUESTED, SettlementBatchStatus.PROCESSING)
    )) {
      throw new ReceivableNotFoundException("Settlement already requested");
    }
    if (receivableRepository.findByDeveloperIdAndStatusOrderByCreatedAtAsc(
      developerId,
      ReceivableStatus.UNSETTLED
    ).isEmpty()) {
      throw new ReceivableNotFoundException("No unsettled receivables to settle");
    }

    SettlementBatch batch = new SettlementBatch();
    batch.setDeveloperId(developerId);
    batch.setRequestedBy(requestedBy);
    batch.setStatus(SettlementBatchStatus.REQUESTED);
    batch.setCurrency(developer.getSettlementCurrency());
    batch.setTotalAmount(BigDecimal.ZERO);

    SettlementBatch saved = batchRepository.save(batch);

    ReceivableReservationService.ReservationResult reservation =
      reservationService.reserveReceivables(developerId, saved.getId());

    if (reservation.getReceivables().isEmpty()) {
      return saved;
    }

    saved.setTotalAmount(reservation.getTotalAmount());
    batchRepository.save(saved);
    return saved;
  }

  @Transactional
  public SettlementBatch approveSettlement(UUID batchId) {
    SettlementBatch batch = batchRepository.findById(batchId)
      .orElseThrow(() -> new ReceivableNotFoundException("Settlement batch not found"));
    if (batch.getStatus() != SettlementBatchStatus.REQUESTED) {
      throw new ReceivableNotFoundException("Settlement is not pending approval");
    }
    batch.setStatus(SettlementBatchStatus.PROCESSING);
    batch.setFailureReason(null);
    SettlementBatch saved = batchRepository.save(batch);
    rabbitTemplate.convertAndSend(RabbitMQConfig.SETTLEMENT_QUEUE, saved.getId().toString());
    return saved;
  }

  @Transactional
  public SettlementBatch rejectSettlement(UUID batchId, String reason) {
    SettlementBatch batch = batchRepository.findById(batchId)
      .orElseThrow(() -> new ReceivableNotFoundException("Settlement batch not found"));
    if (batch.getStatus() != SettlementBatchStatus.REQUESTED) {
      throw new ReceivableNotFoundException("Settlement is not pending approval");
    }
    batch.setStatus(SettlementBatchStatus.REJECTED);
    batch.setFailureReason(reason == null || reason.isBlank() ? "Rejected by admin" : reason);
    batch.setProcessedAt(OffsetDateTime.now());
    SettlementBatch saved = batchRepository.save(batch);
    reservationService.releaseReservations(batchId);
    return saved;
  }

  @Transactional
  public void processSettlementBatch(UUID batchId) {
    SettlementBatch batch = batchRepository.findById(batchId)
      .orElseThrow(() -> new ReceivableNotFoundException("Settlement batch not found"));
    if (batch.getStatus() == SettlementBatchStatus.REJECTED || batch.getStatus() == SettlementBatchStatus.PAID) {
      throw new ReceivableNotFoundException("Settlement is not eligible for processing");
    }

    List<Receivable> receivables = receivableRepository.findBySettlementBatchId(batchId);
    if (receivables.isEmpty()) {
      throw new ReceivableNotFoundException("No receivables found for batch");
    }

    Developer developer = developerRepository.findById(batch.getDeveloperId())
      .orElseThrow(() -> new ReceivableNotFoundException("Developer not found"));

    batch.setStatus(SettlementBatchStatus.PROCESSING);
    batchRepository.save(batch);

    try {
      PayoutRequest request = new PayoutRequest();
      request.setBankAccountRef(developer.getBankAccountRef());
      request.setAmount(batch.getTotalAmount());
      request.setCurrency(batch.getCurrency());
      bankAdapterClient.sendPayout(request);

      OffsetDateTime now = OffsetDateTime.now();
      for (Receivable receivable : receivables) {
        receivable.setStatus(ReceivableStatus.SETTLED);
        receivable.setSettledAt(now);
      }
      receivableRepository.saveAll(receivables);

      batch.setStatus(SettlementBatchStatus.PAID);
      batch.setProcessedAt(now);
      batchRepository.save(batch);
    } catch (Exception ex) {
      batch.setStatus(SettlementBatchStatus.FAILED);
      batch.setFailureReason(ex.getMessage());
      batch.setProcessedAt(OffsetDateTime.now());
      batchRepository.save(batch);
    }
  }
}
