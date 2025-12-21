package com.mgx.wallet.repository;

import com.mgx.wallet.model.Wallet;
import com.mgx.wallet.model.WalletType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
  Optional<Wallet> findByUserIdAndTypeAndGameId(UUID userId, WalletType type, UUID gameId);
}
