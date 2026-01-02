package com.mgx.banklink.service;

import com.mgx.banklink.model.BankLink;
import com.mgx.banklink.repository.BankLinkRepository;
import com.mgx.otp.model.OtpSession;
import com.mgx.otp.service.OtpService;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class BankLinkService {
  private final BankLinkRepository bankLinkRepository;
  private final OtpService otpService;

  public BankLinkService(BankLinkRepository bankLinkRepository, OtpService otpService) {
    this.bankLinkRepository = bankLinkRepository;
    this.otpService = otpService;
  }

  public BankLink linkBank(UUID userId, String bankRef, String phoneNumber, UUID otpSessionId) {
    if (bankRef == null || bankRef.isBlank()) {
      throw new IllegalArgumentException("bankRef is required");
    }
    if (phoneNumber == null || phoneNumber.isBlank()) {
      throw new IllegalArgumentException("phoneNumber is required");
    }
    if (otpSessionId == null) {
      throw new IllegalArgumentException("otpSessionId is required");
    }

    OtpSession session = otpService.getVerifiedSession(otpSessionId);
    if (!phoneNumber.equals(session.getPhoneNumber())) {
      throw new IllegalArgumentException("Phone number does not match OTP session");
    }

    BankLink link = bankLinkRepository.findByUserId(userId).orElse(new BankLink());
    link.setUserId(userId);
    link.setBankRef(bankRef);
    link.setPhoneNumber(phoneNumber);
    link.setVerifiedAt(OffsetDateTime.now());
    return bankLinkRepository.save(link);
  }
}
