package com.mgx.otp.model;

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
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "otp_sessions")
public class OtpSession {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "phone_number", nullable = false)
  private String phoneNumber;

  @Enumerated(EnumType.STRING)
  @Column(name = "purpose", nullable = false)
  private OtpPurpose purpose;

  @Column(name = "code_hash", nullable = false)
  private String codeHash;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private OtpStatus status;

  @Column(name = "attempts", nullable = false)
  private int attempts;

  @Column(name = "expires_at", nullable = false)
  private OffsetDateTime expiresAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public OtpPurpose getPurpose() {
    return purpose;
  }

  public void setPurpose(OtpPurpose purpose) {
    this.purpose = purpose;
  }

  public String getCodeHash() {
    return codeHash;
  }

  public void setCodeHash(String codeHash) {
    this.codeHash = codeHash;
  }

  public OtpStatus getStatus() {
    return status;
  }

  public void setStatus(OtpStatus status) {
    this.status = status;
  }

  public int getAttempts() {
    return attempts;
  }

  public void setAttempts(int attempts) {
    this.attempts = attempts;
  }

  public OffsetDateTime getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(OffsetDateTime expiresAt) {
    this.expiresAt = expiresAt;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
