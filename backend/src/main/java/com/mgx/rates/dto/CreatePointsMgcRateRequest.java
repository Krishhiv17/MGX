package com.mgx.rates.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class CreatePointsMgcRateRequest {
  private BigDecimal pointsPerMgc;
  private OffsetDateTime activeFrom;

  public BigDecimal getPointsPerMgc() {
    return pointsPerMgc;
  }

  public void setPointsPerMgc(BigDecimal pointsPerMgc) {
    this.pointsPerMgc = pointsPerMgc;
  }

  public OffsetDateTime getActiveFrom() {
    return activeFrom;
  }

  public void setActiveFrom(OffsetDateTime activeFrom) {
    this.activeFrom = activeFrom;
  }
}
