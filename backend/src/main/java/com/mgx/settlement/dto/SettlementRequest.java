package com.mgx.settlement.dto;

import java.util.UUID;

public class SettlementRequest {
  private UUID developerId;

  public UUID getDeveloperId() {
    return developerId;
  }

  public void setDeveloperId(UUID developerId) {
    this.developerId = developerId;
  }
}
