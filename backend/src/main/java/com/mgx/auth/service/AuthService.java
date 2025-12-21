package com.mgx.auth.service;

import com.mgx.auth.dto.AuthResponse;
import com.mgx.auth.security.JwtTokenProvider;
import com.mgx.auth.security.JwtUserPrincipal;
import com.mgx.user.model.User;
import com.mgx.user.model.UserRole;
import com.mgx.user.repository.UserRepository;
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

  public AuthService(
    UserRepository userRepository,
    PasswordEncoder passwordEncoder,
    JwtTokenProvider tokenProvider,
    WalletInitializationService walletInitializationService
  ) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.tokenProvider = tokenProvider;
    this.walletInitializationService = walletInitializationService;
  }

  public AuthResponse register(String email, String password, UserRole role) {
    Optional<User> existing = userRepository.findByEmail(email);
    if (existing.isPresent()) {
      throw new IllegalArgumentException("Email already registered");
    }

    User user = new User();
    user.setEmail(email);
    user.setPasswordHash(passwordEncoder.encode(password));
    user.setRole(role == null ? UserRole.USER : role);

    User saved = userRepository.save(user);
    walletInitializationService.initializeWallets(saved.getId());

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
