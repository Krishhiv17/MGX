package com.mgx.purchase.service;

import com.mgx.common.exception.GameNotFoundException;
import com.mgx.common.exception.IdempotencyConflictException;
import com.mgx.common.exception.RateNotFoundException;
import com.mgx.common.service.IdempotencyService;
import com.mgx.common.util.ValidationUtil;
import com.mgx.fx.model.FxRate;
import com.mgx.fx.model.FxRateWindow;
import com.mgx.fx.service.FxService;
import com.mgx.game.model.Game;
import com.mgx.game.repository.GameRepository;
import com.mgx.ledger.model.AssetType;
import com.mgx.ledger.model.LedgerDirection;
import com.mgx.ledger.model.LedgerRefType;
import com.mgx.ledger.service.LedgerService;
import com.mgx.purchase.model.Purchase;
import com.mgx.purchase.model.PurchaseStatus;
import com.mgx.purchase.repository.PurchaseRepository;
import com.mgx.rates.model.RateMgcUgc;
import com.mgx.rates.service.RateService;
import com.mgx.settlement.model.Receivable;
import com.mgx.settlement.model.ReceivableStatus;
import com.mgx.settlement.repository.ReceivableRepository;
import com.mgx.wallet.model.Wallet;
import com.mgx.wallet.model.WalletType;
import com.mgx.wallet.service.WalletService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseService {
  private static final int SCALE = 12;

  private final PurchaseRepository purchaseRepository;
  private final RateService rateService;
  private final WalletService walletService;
  private final LedgerService ledgerService;
  private final GameRepository gameRepository;
  private final FxService fxService;
  private final ReceivableRepository receivableRepository;
  private final IdempotencyService idempotencyService;

  public PurchaseService(
    PurchaseRepository purchaseRepository,
    RateService rateService,
    WalletService walletService,
    LedgerService ledgerService,
    GameRepository gameRepository,
    FxService fxService,
    ReceivableRepository receivableRepository,
    IdempotencyService idempotencyService
  ) {
    this.purchaseRepository = purchaseRepository;
    this.rateService = rateService;
    this.walletService = walletService;
    this.ledgerService = ledgerService;
    this.gameRepository = gameRepository;
    this.fxService = fxService;
    this.receivableRepository = receivableRepository;
    this.idempotencyService = idempotencyService;
  }

  @Transactional
  public Purchase createPurchase(
    UUID userId,
    UUID gameId,
    BigDecimal mgcAmount,
    BigDecimal ugcAmount,
    String idempotencyKey
  ) {
    if (idempotencyKey == null || idempotencyKey.isBlank()) {
      throw new IllegalArgumentException("Idempotency-Key header is required");
    }
    if (gameId == null) {
      throw new IllegalArgumentException("gameId is required");
    }
    if ((mgcAmount == null && ugcAmount == null) || (mgcAmount != null && ugcAmount != null)) {
      throw new IllegalArgumentException("Provide either mgcAmount or ugcAmount");
    }

    Optional<Purchase> existing = purchaseRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey);
    if (existing.isPresent()) {
      return existing.get();
    }

    String redisKey = idempotencyService.buildKey("purchase", userId.toString(), idempotencyKey);
    if (!idempotencyService.checkAndStore(redisKey, "PENDING")) {
      throw new IdempotencyConflictException("Duplicate idempotency key");
    }

    Game game = gameRepository.findById(gameId)
      .orElseThrow(() -> new GameNotFoundException("Game not found"));

    RateMgcUgc rate = rateService.getActiveMgcToUgcRate(gameId);
    BigDecimal ugcPerMgc = rate.getUgcPerMgc();

    BigDecimal mgcSpent;
    BigDecimal ugcCredited;

    if (mgcAmount != null) {
      ValidationUtil.requirePositive(mgcAmount, "mgcAmount");
      mgcSpent = mgcAmount.setScale(SCALE, RoundingMode.HALF_UP);
      ugcCredited = mgcSpent.multiply(ugcPerMgc).setScale(SCALE, RoundingMode.HALF_UP);
    } else {
      ValidationUtil.requirePositive(ugcAmount, "ugcAmount");
      ugcCredited = ugcAmount.setScale(SCALE, RoundingMode.HALF_UP);
      mgcSpent = ugcCredited.divide(ugcPerMgc, SCALE, RoundingMode.HALF_UP);
    }

    Wallet mgcWallet = walletService.getWalletByUserAndType(userId, WalletType.MGC, null);
    Wallet ugcWallet = walletService.getOrCreateWallet(userId, WalletType.UGC, gameId);

    Purchase purchase = new Purchase();
    purchase.setUserId(userId);
    purchase.setGameId(gameId);
    purchase.setMgcSpent(mgcSpent);
    purchase.setUgcCredited(ugcCredited);
    purchase.setRateUgcPerMgcSnapshot(ugcPerMgc);
    purchase.setRateId(rate.getId());
    purchase.setStatus(PurchaseStatus.COMPLETED);
    purchase.setIdempotencyKey(idempotencyKey);

    Purchase saved = purchaseRepository.save(purchase);

    walletService.updateBalance(mgcWallet.getId(), mgcSpent, LedgerDirection.DEBIT);
    walletService.updateBalance(ugcWallet.getId(), ugcCredited, LedgerDirection.CREDIT);

    ledgerService.createEntry(
      LedgerRefType.PURCHASE,
      saved.getId(),
      mgcWallet.getId(),
      LedgerDirection.DEBIT,
      AssetType.MGC,
      mgcSpent
    );

    ledgerService.createEntry(
      LedgerRefType.PURCHASE,
      saved.getId(),
      ugcWallet.getId(),
      LedgerDirection.CREDIT,
      AssetType.UGC,
      ugcCredited
    );

    createReceivable(game, saved, mgcSpent);

    idempotencyService.storeResult(redisKey, saved.getId().toString());
    return saved;
  }

  private void createReceivable(Game game, Purchase purchase, BigDecimal mgcSpent) {
    String settlementCurrency = game.getSettlementCurrency();
    ValidationUtil.requireCurrency(settlementCurrency);

    FxRateWindow window = fxService.getCurrentFxWindow();
    List<FxRate> rates = fxService.getFxRatesForWindow(window.getId());

    BigDecimal fxRateUsed = BigDecimal.ONE;
    if (!"USD".equals(settlementCurrency)) {
      fxRateUsed = rates.stream()
        .filter(rate -> settlementCurrency.equals(rate.getQuoteCurrency()))
        .map(FxRate::getRate)
        .findFirst()
        .orElseThrow(() -> new RateNotFoundException("FX rate not found"));
    }

    BigDecimal amountDue = mgcSpent.multiply(fxRateUsed).setScale(SCALE, RoundingMode.HALF_UP);

    Receivable receivable = new Receivable();
    receivable.setPurchaseId(purchase.getId());
    receivable.setDeveloperId(game.getDeveloperId());
    receivable.setAmountDue(amountDue);
    receivable.setSettlementCurrency(settlementCurrency);
    receivable.setFxWindowId(window.getId());
    receivable.setFxRateUsed(fxRateUsed);
    receivable.setStatus(ReceivableStatus.UNSETTLED);
    receivable.setReservedAt(null);
    receivable.setSettledAt(null);
    receivable.setSettlementBatchId(null);
    receivable.setCreatedAt(OffsetDateTime.now());

    receivableRepository.save(receivable);
  }

  public List<Purchase> listPurchases(UUID userId) {
    return purchaseRepository.findByUserIdOrderByCreatedAtDesc(userId);
  }
}
