package com.mgx.ledger.service;

import com.mgx.ledger.model.AssetType;
import com.mgx.ledger.model.LedgerDirection;
import com.mgx.ledger.model.LedgerEntry;
import com.mgx.ledger.model.LedgerRefType;
import com.mgx.ledger.repository.LedgerEntryRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class LedgerService {
  private final LedgerEntryRepository ledgerEntryRepository;

  public LedgerService(LedgerEntryRepository ledgerEntryRepository) {
    this.ledgerEntryRepository = ledgerEntryRepository;
  }

  public LedgerEntry createEntry(
    LedgerRefType refType,
    UUID refId,
    UUID walletId,
    LedgerDirection direction,
    AssetType assetType,
    BigDecimal amount
  ) {
    LedgerEntry entry = new LedgerEntry();
    entry.setRefType(refType);
    entry.setRefId(refId);
    entry.setWalletId(walletId);
    entry.setDirection(direction);
    entry.setAssetType(assetType);
    entry.setAmount(amount);
    return ledgerEntryRepository.save(entry);
  }

  public List<LedgerEntry> getLedgerEntriesByWallet(UUID walletId) {
    return ledgerEntryRepository.findByWalletIdOrderByCreatedAtDesc(walletId);
  }

  public List<LedgerEntry> getLedgerEntriesByReference(LedgerRefType refType, UUID refId) {
    return ledgerEntryRepository.findByRefTypeAndRefIdOrderByCreatedAtDesc(refType, refId);
  }
}
