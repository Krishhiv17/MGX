package com.mgx.fx.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "fx_rate_windows")
public class FxRateWindow {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "provider", nullable = false)
  private String provider;

  @Column(name = "fetched_at", nullable = false)
  private OffsetDateTime fetchedAt;

  @Column(name = "valid_from", nullable = false)
  private OffsetDateTime validFrom;

  @Column(name = "valid_to", nullable = false)
  private OffsetDateTime validTo;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private FxWindowStatus status;

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
