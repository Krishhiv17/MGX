package com.mgx.wallet.service;

import com.mgx.common.exception.InsufficientBalanceException;
import com.mgx.common.exception.WalletNotFoundException;
import com.mgx.ledger.model.LedgerDirection;
import com.mgx.wallet.model.Wallet;
import com.mgx.wallet.model.WalletType;
import com.mgx.wallet.repository.WalletRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
public class WalletService {
  private static final int MAX_RETRIES = 3;

  private final WalletRepository walletRepository;

  public WalletService(WalletRepository walletRepository) {
    this.walletRepository = walletRepository;
  }

  public List<Wallet> getWalletsByUserId(UUID userId, String countryCode) {
    return walletRepository.findByUserIdAndCountryCode(userId, countryCode);
  }

  public Wallet getWalletById(UUID walletId) {
    return walletRepository.findById(walletId)
      .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
  }

  public Wallet getWalletByUserAndType(UUID userId, WalletType type, UUID gameId, String countryCode) {
    return walletRepository.findByUserIdAndTypeAndGameIdAndCountryCode(userId, type, gameId, countryCode)
      .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
  }

  public Wallet getOrCreateWallet(UUID userId, WalletType type, UUID gameId, String countryCode) {
    return walletRepository.findByUserIdAndTypeAndGameIdAndCountryCode(userId, type, gameId, countryCode)
      .orElseGet(() -> {
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setType(type);
        wallet.setGameId(gameId);
        wallet.setCountryCode(countryCode);
        wallet.setBalance(BigDecimal.ZERO);
        return walletRepository.save(wallet);
      });
  }

  public Wallet updateBalance(UUID walletId, BigDecimal amount, LedgerDirection direction) {
    for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
      try {
        Wallet wallet = getWalletById(walletId);
        BigDecimal current = wallet.getBalance();
        BigDecimal updated = direction == LedgerDirection.DEBIT
          ? current.subtract(amount)
          : current.add(amount);

        if (updated.signum() < 0) {
          throw new InsufficientBalanceException("Insufficient balance");
        }

        wallet.setBalance(updated);
        return walletRepository.save(wallet);
      } catch (OptimisticLockingFailureException ex) {
        if (attempt == MAX_RETRIES - 1) {
          throw ex;
        }
      }
    }

    throw new IllegalStateException("Failed to update wallet balance");
  }

  public Wallet setBalance(UUID walletId, BigDecimal newBalance) {
    for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
      try {
        Wallet wallet = getWalletById(walletId);
        wallet.setBalance(newBalance);
        return walletRepository.save(wallet);
      } catch (OptimisticLockingFailureException ex) {
        if (attempt == MAX_RETRIES - 1) {
          throw ex;
        }
      }
    }

    throw new IllegalStateException("Failed to set wallet balance");
  }
}
