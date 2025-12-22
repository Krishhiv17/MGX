package com.mgx.fx.dto;

import com.mgx.fx.model.FxRateWindow;
import com.mgx.fx.model.FxWindowStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public class FxWindowResponse {
  private UUID id;
  private String provider;
  private OffsetDateTime fetchedAt;
  private OffsetDateTime validFrom;
  private OffsetDateTime validTo;
  private FxWindowStatus status;

  public static FxWindowResponse from(FxRateWindow window) {
    FxWindowResponse response = new FxWindowResponse();
    response.setId(window.getId());
    response.setProvider(window.getProvider());
    response.setFetchedAt(window.getFetchedAt());
    response.setValidFrom(window.getValidFrom());
    response.setValidTo(window.getValidTo());
    response.setStatus(window.getStatus());
    return response;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public OffsetDateTime getFetchedAt() {
    return fetchedAt;
  }

  public void setFetchedAt(OffsetDateTime fetchedAt) {
    this.fetchedAt = fetchedAt;
  }

  public OffsetDateTime getValidFrom() {
    return validFrom;
  }

  public void setValidFrom(OffsetDateTime validFrom) {
    this.validFrom = validFrom;
  }

  public OffsetDateTime getValidTo() {
    return validTo;
  }

  public void setValidTo(OffsetDateTime validTo) {
    this.validTo = validTo;
  }

  public FxWindowStatus getStatus() {
    return status;
  }

  public void setStatus(FxWindowStatus status) {
    this.status = status;
  }
}
