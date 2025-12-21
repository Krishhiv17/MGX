package com.mgx.ledger.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(name = "ref_type", nullable = false)
  private LedgerRefType refType;

  @Column(name = "ref_id", nullable = false)
  private UUID refId;

  @Column(name = "wallet_id", nullable = false)
  private UUID walletId;

  @Enumerated(EnumType.STRING)
  @Column(name = "direction", nullable = false)
  private LedgerDirection direction;

  @Enumerated(EnumType.STRING)
  @Column(name = "asset_type", nullable = false)
  private AssetType assetType;

  @Column(name = "amount", nullable = false, precision = 38, scale = 12)
  private BigDecimal amount;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public LedgerRefType getRefType() {
    return refType;
  }

  public void setRefType(LedgerRefType refType) {
    this.refType = refType;
  }

  public UUID getRefId() {
    return refId;
  }

  public void setRefId(UUID refId) {
    this.refId = refId;
  }

  public UUID getWalletId() {
    return walletId;
  }

  public void setWalletId(UUID walletId) {
    this.walletId = walletId;
  }

  public LedgerDirection getDirection() {
    return direction;
  }

  public void setDirection(LedgerDirection direction) {
    this.direction = direction;
  }

  public AssetType getAssetType() {
    return assetType;
  }

  public void setAssetType(AssetType assetType) {
    this.assetType = assetType;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
