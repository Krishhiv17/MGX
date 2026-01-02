package com.mgx.profile.service;

import com.mgx.profile.dto.ProfileResponse;
import com.mgx.supportedcountry.model.SupportedCountry;
import com.mgx.supportedcountry.repository.SupportedCountryRepository;
import com.mgx.user.model.User;
import com.mgx.user.repository.UserRepository;
import com.mgx.wallet.model.WalletType;
import com.mgx.wallet.service.WalletService;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {
  private final UserRepository userRepository;
  private final SupportedCountryRepository supportedCountryRepository;
  private final WalletService walletService;

  public ProfileService(
    UserRepository userRepository,
    SupportedCountryRepository supportedCountryRepository,
    WalletService walletService
  ) {
    this.userRepository = userRepository;
    this.supportedCountryRepository = supportedCountryRepository;
    this.walletService = walletService;
  }

  public ProfileResponse getProfile(UUID userId) {
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new IllegalArgumentException("User not found"));
    return ProfileResponse.from(user);
  }

  @Transactional
  public ProfileResponse updateCountry(UUID userId, String countryCode) {
    if (countryCode == null || countryCode.isBlank()) {
      throw new IllegalArgumentException("Country code is required");
    }
    SupportedCountry country = supportedCountryRepository.findById(countryCode)
      .orElseThrow(() -> new IllegalArgumentException("Country is not supported"));
    if (!"ACTIVE".equalsIgnoreCase(country.getStatus())) {
      throw new IllegalArgumentException("Country is not supported");
    }

    User user = userRepository.findById(userId)
      .orElseThrow(() -> new IllegalArgumentException("User not found"));
    if (!countryCode.equalsIgnoreCase(user.getCountryCode())) {
      user.setCountryCode(countryCode);
      userRepository.save(user);
      walletService.getOrCreateWallet(userId, WalletType.REWARD_POINTS, null, countryCode);
      walletService.getOrCreateWallet(userId, WalletType.MGC, null, countryCode);
    }

    return ProfileResponse.from(user);
  }
}
