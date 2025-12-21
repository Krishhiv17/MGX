package com.mgx.ledger.repository;

import com.mgx.ledger.model.LedgerEntry;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {
  List<LedgerEntry> findByWalletIdOrderByCreatedAtDesc(UUID walletId);
}
