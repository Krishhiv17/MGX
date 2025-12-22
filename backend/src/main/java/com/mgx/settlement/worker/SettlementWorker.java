package com.mgx.settlement.worker;

import com.mgx.settlement.service.SettlementService;
import java.util.UUID;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SettlementWorker {
  private final SettlementService settlementService;

  public SettlementWorker(SettlementService settlementService) {
    this.settlementService = settlementService;
  }

  @RabbitListener(queues = "settlement.batch.requested")
  public void handle(String batchId) {
    settlementService.processSettlementBatch(UUID.fromString(batchId));
  }
}
