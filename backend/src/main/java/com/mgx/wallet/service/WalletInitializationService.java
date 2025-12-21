package com.mgx.wallet.service;

import com.mgx.wallet.model.Wallet;
import com.mgx.wallet.model.WalletType;
import com.mgx.wallet.repository.WalletRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class WalletInitializationService {
  private final WalletRepository walletRepository;

  public WalletInitializationService(WalletRepository walletRepository) {
    this.walletRepository = walletRepository;
  }

  public void initializeWallets(UUID userId) {
    createWallet(userId, WalletType.REWARD_POINTS);
    createWallet(userId, WalletType.MGC);
  }

  private void createWallet(UUID userId, WalletType type) {
    Wallet wallet = new Wallet();
    wallet.setUserId(userId);
    wallet.setType(type);
    wallet.setGameId(null);
    wallet.setBalance(BigDecimal.ZERO);
    walletRepository.save(wallet);
  }
}
