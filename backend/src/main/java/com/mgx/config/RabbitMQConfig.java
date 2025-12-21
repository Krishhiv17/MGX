package com.mgx.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
  public static final String SETTLEMENT_QUEUE = "settlement.batch.requested";
  public static final String SETTLEMENT_DLQ = "settlement.batch.dlq";

  @Bean
  public Queue settlementQueue() {
    return new Queue(SETTLEMENT_QUEUE, true);
  }

  @Bean
  public Queue settlementDlq() {
    return new Queue(SETTLEMENT_DLQ, true);
  }
}
