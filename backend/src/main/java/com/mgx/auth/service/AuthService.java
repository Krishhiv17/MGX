package com.mgx.auth.service;

import com.mgx.auth.dto.AuthResponse;
import com.mgx.auth.security.JwtTokenProvider;
import com.mgx.auth.security.JwtUserPrincipal;
import com.mgx.user.model.User;
import com.mgx.user.model.UserRole;
import com.mgx.user.repository.UserRepository;
import com.mgx.supportedcountry.repository.SupportedCountryRepository;
import com.mgx.wallet.service.WalletInitializationService;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider tokenProvider;
  private final WalletInitializationService walletInitializationService;
  private final SupportedCountryRepository supportedCountryRepository;

  public AuthService(
    UserRepository userRepository,
    PasswordEncoder passwordEncoder,
    JwtTokenProvider tokenProvider,
    WalletInitializationService walletInitializationService,
    SupportedCountryRepository supportedCountryRepository
  ) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.tokenProvider = tokenProvider;
    this.walletInitializationService = walletInitializationService;
    this.supportedCountryRepository = supportedCountryRepository;
  }

  public AuthResponse register(
    String email,
    String password,
    UserRole role,
    String phoneNumber,
    String countryCode
  ) {
    Optional<User> existing = userRepository.findByEmail(email);
    if (existing.isPresent()) {
      throw new IllegalArgumentException("Email already registered");
    }
    if (phoneNumber == null || phoneNumber.isBlank()) {
      throw new IllegalArgumentException("Phone number is required");
    }
    if (userRepository.findByPhoneNumber(phoneNumber).isPresent()) {
      throw new IllegalArgumentException("Phone number already registered");
    }
    if (countryCode == null || countryCode.isBlank()) {
      throw new IllegalArgumentException("Country code is required");
    }
    boolean supported = supportedCountryRepository.findById(countryCode)
      .map(country -> "ACTIVE".equalsIgnoreCase(country.getStatus()))
      .orElse(false);
    if (!supported) {
      throw new IllegalArgumentException("Country is not supported");
    }

    User user = new User();
    user.setEmail(email);
    user.setPasswordHash(passwordEncoder.encode(password));
    user.setRole(role == null ? UserRole.USER : role);
    user.setPhoneNumber(phoneNumber);
    user.setCountryCode(countryCode);

    User saved = userRepository.save(user);
    walletInitializationService.initializeWallets(saved.getId(), saved.getCountryCode());

    String token = tokenProvider.generateToken(saved);
    return new AuthResponse(token, saved.getId(), saved.getEmail(), saved.getRole());
  }

  public AuthResponse login(String email, String password) {
    User user = userRepository.findByEmail(email)
      .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

    if (!passwordEncoder.matches(password, user.getPasswordHash())) {
      throw new IllegalArgumentException("Invalid credentials");
    }

    String token = tokenProvider.generateToken(user);
    return new AuthResponse(token, user.getId(), user.getEmail(), user.getRole());
  }

  public JwtUserPrincipal validateToken(String token) {
    if (!tokenProvider.validateToken(token)) {
      throw new IllegalArgumentException("Invalid token");
    }
    return tokenProvider.getPrincipal(token);
  }
}
