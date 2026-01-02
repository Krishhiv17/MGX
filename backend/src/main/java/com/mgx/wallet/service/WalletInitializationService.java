package com.mgx.wallet.service;

import com.mgx.bank.client.BankPointsClient;
import com.mgx.bank.dto.PointsBalanceResponse;
import com.mgx.wallet.model.Wallet;
import com.mgx.wallet.model.WalletType;
import com.mgx.wallet.repository.WalletRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service
public class WalletInitializationService {
  private final WalletRepository walletRepository;
  private final BankPointsClient bankPointsClient;
  private final BigDecimal defaultPoints;

  public WalletInitializationService(
    WalletRepository walletRepository,
    BankPointsClient bankPointsClient,
    @Value("${mgx.bank.default-points:1000000}") BigDecimal defaultPoints
  ) {
    this.walletRepository = walletRepository;
    this.bankPointsClient = bankPointsClient;
    this.defaultPoints = defaultPoints;
  }

  public void initializeWallets(UUID userId, String countryCode) {
    BigDecimal initialPoints = fetchInitialPoints(userId);
    createWallet(userId, WalletType.REWARD_POINTS, countryCode, initialPoints);
    createWallet(userId, WalletType.MGC, countryCode);
  }

  private BigDecimal fetchInitialPoints(UUID userId) {
    try {
      PointsBalanceResponse response = bankPointsClient.getPoints(userId);
      if (response != null && response.getPointsAvailable() != null) {
        return response.getPointsAvailable();
      }
    } catch (RestClientException ex) {
      // Bank mock may be down; use default points for local testing.
    }
    return defaultPoints;
  }

  private void createWallet(UUID userId, WalletType type, String countryCode) {
    createWallet(userId, type, countryCode, BigDecimal.ZERO);
  }

  private void createWallet(UUID userId, WalletType type, String countryCode, BigDecimal balance) {
    Wallet wallet = new Wallet();
    wallet.setUserId(userId);
    wallet.setType(type);
    wallet.setGameId(null);
    wallet.setCountryCode(countryCode);
    wallet.setBalance(balance);
    walletRepository.save(wallet);
  }
}
