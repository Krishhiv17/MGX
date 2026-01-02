package com.mgx.otp.service;

import com.mgx.otp.dto.OtpResponse;
import com.mgx.otp.dto.OtpVerifyResponse;
import com.mgx.otp.model.OtpPurpose;
import com.mgx.otp.model.OtpSession;
import com.mgx.otp.model.OtpStatus;
import com.mgx.otp.repository.OtpSessionRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class OtpService {
  private static final int MAX_ATTEMPTS = 5;
  private static final int EXPIRY_MINUTES = 5;
  private final OtpSessionRepository otpSessionRepository;
  private final Random random = new Random();

  public OtpService(OtpSessionRepository otpSessionRepository) {
    this.otpSessionRepository = otpSessionRepository;
  }

  public OtpResponse requestOtp(String phoneNumber, OtpPurpose purpose) {
    if (phoneNumber == null || phoneNumber.isBlank()) {
      throw new IllegalArgumentException("phoneNumber is required");
    }
    if (purpose == null) {
      throw new IllegalArgumentException("purpose is required");
    }

    String code = String.format("%06d", random.nextInt(1_000_000));
    OtpSession session = new OtpSession();
    session.setPhoneNumber(phoneNumber);
    session.setPurpose(purpose);
    session.setCodeHash(hash(code));
    session.setStatus(OtpStatus.PENDING);
    session.setAttempts(0);
    session.setExpiresAt(OffsetDateTime.now().plusMinutes(EXPIRY_MINUTES));
    OtpSession saved = otpSessionRepository.save(session);
    return new OtpResponse(saved.getId(), saved.getExpiresAt(), code);
  }

  public OtpVerifyResponse verifyOtp(UUID sessionId, String code) {
    if (sessionId == null) {
      throw new IllegalArgumentException("sessionId is required");
    }
    if (code == null || code.isBlank()) {
      throw new IllegalArgumentException("code is required");
    }

    Optional<OtpSession> sessionOpt = otpSessionRepository.findById(sessionId);
    if (sessionOpt.isEmpty()) {
      return new OtpVerifyResponse(OtpStatus.FAILED, "Invalid session");
    }
    OtpSession session = sessionOpt.get();

    if (session.getStatus() != OtpStatus.PENDING) {
      return new OtpVerifyResponse(session.getStatus(), "Session already processed");
    }
    if (session.getExpiresAt().isBefore(OffsetDateTime.now())) {
      session.setStatus(OtpStatus.EXPIRED);
      otpSessionRepository.save(session);
      return new OtpVerifyResponse(OtpStatus.EXPIRED, "OTP expired");
    }

    if (!hash(code).equals(session.getCodeHash())) {
      session.setAttempts(session.getAttempts() + 1);
      if (session.getAttempts() >= MAX_ATTEMPTS) {
        session.setStatus(OtpStatus.FAILED);
      }
      otpSessionRepository.save(session);
      return new OtpVerifyResponse(session.getStatus(), "Invalid code");
    }

    session.setStatus(OtpStatus.VERIFIED);
    otpSessionRepository.save(session);
    return new OtpVerifyResponse(OtpStatus.VERIFIED, "Verified");
  }

  public OtpSession getVerifiedSession(UUID sessionId) {
    OtpSession session = otpSessionRepository.findById(sessionId)
      .orElseThrow(() -> new IllegalArgumentException("OTP session not found"));
    if (session.getStatus() != OtpStatus.VERIFIED) {
      throw new IllegalArgumentException("OTP not verified");
    }
    return session;
  }

  private String hash(String code) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashed = digest.digest(code.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hashed);
    } catch (Exception ex) {
      throw new IllegalStateException("Failed to hash OTP");
    }
  }
}
