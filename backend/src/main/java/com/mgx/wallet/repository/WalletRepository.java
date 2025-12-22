package com.mgx.wallet.repository;

import com.mgx.wallet.model.Wallet;
import com.mgx.wallet.model.WalletType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
  List<Wallet> findByUserId(UUID userId);

  Optional<Wallet> findByUserIdAndTypeAndGameId(UUID userId, WalletType type, UUID gameId);
}
