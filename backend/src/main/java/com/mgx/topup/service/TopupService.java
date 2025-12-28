package com.mgx.topup.service;

import com.mgx.bank.client.BankPointsClient;
import com.mgx.bank.dto.PointsBalanceResponse;
import com.mgx.common.exception.IdempotencyConflictException;
import com.mgx.common.exception.InsufficientBalanceException;
import com.mgx.common.util.ValidationUtil;
import com.mgx.common.service.IdempotencyService;
import com.mgx.ledger.model.AssetType;
import com.mgx.ledger.model.LedgerDirection;
import com.mgx.ledger.model.LedgerRefType;
import com.mgx.ledger.service.LedgerService;
import com.mgx.rates.model.RatePointsMgc;
import com.mgx.rates.service.RateService;
import com.mgx.topup.model.Topup;
import com.mgx.topup.model.TopupStatus;
import com.mgx.topup.repository.TopupRepository;
import com.mgx.wallet.model.Wallet;
import com.mgx.wallet.model.WalletType;
import com.mgx.wallet.service.WalletService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TopupService {
  private static final int SCALE = 12;

  private final TopupRepository topupRepository;
  private final RateService rateService;
  private final WalletService walletService;
  private final LedgerService ledgerService;
  private final IdempotencyService idempotencyService;
  private final BankPointsClient bankPointsClient;

  public TopupService(
    TopupRepository topupRepository,
    RateService rateService,
    WalletService walletService,
    LedgerService ledgerService,
    IdempotencyService idempotencyService,
    BankPointsClient bankPointsClient
  ) {
    this.topupRepository = topupRepository;
    this.rateService = rateService;
    this.walletService = walletService;
    this.ledgerService = ledgerService;
    this.idempotencyService = idempotencyService;
    this.bankPointsClient = bankPointsClient;
  }

  @Transactional
  public Topup createTopup(UUID userId, BigDecimal pointsAmount, BigDecimal mgcAmount, String idempotencyKey) {
    if (idempotencyKey == null || idempotencyKey.isBlank()) {
      throw new IllegalArgumentException("Idempotency-Key header is required");
    }
    if ((pointsAmount == null && mgcAmount == null) || (pointsAmount != null && mgcAmount != null)) {
      throw new IllegalArgumentException("Provide either pointsAmount or mgcAmount");
    }

    Optional<Topup> existing = topupRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey);
    if (existing.isPresent()) {
      return existing.get();
    }

    String redisKey = idempotencyService.buildKey("topup", userId.toString(), idempotencyKey);
    if (!idempotencyService.checkAndStore(redisKey, "PENDING")) {
      throw new IdempotencyConflictException("Duplicate idempotency key");
    }

    RatePointsMgc rate = rateService.getActivePointsToMgcRate();
    BigDecimal pointsPerMgc = rate.getPointsPerMgc();

    BigDecimal pointsDebited;
    BigDecimal mgcCredited;

    if (pointsAmount != null) {
      ValidationUtil.requirePositive(pointsAmount, "pointsAmount");
      pointsDebited = pointsAmount.setScale(SCALE, RoundingMode.HALF_UP);
      mgcCredited = pointsDebited.divide(pointsPerMgc, SCALE, RoundingMode.HALF_UP);
    } else {
      ValidationUtil.requirePositive(mgcAmount, "mgcAmount");
      mgcCredited = mgcAmount.setScale(SCALE, RoundingMode.HALF_UP);
      pointsDebited = mgcCredited.multiply(pointsPerMgc).setScale(SCALE, RoundingMode.HALF_UP);
    }

    Wallet pointsWallet = walletService.getWalletByUserAndType(userId, WalletType.REWARD_POINTS, null);
    Wallet mgcWallet = walletService.getWalletByUserAndType(userId, WalletType.MGC, null);

    PointsBalanceResponse bankAfterDebit;
    try {
      bankAfterDebit = bankPointsClient.debitPoints(userId, pointsDebited);
    } catch (Exception ex) {
      throw new InsufficientBalanceException("Unable to debit points");
    }
    if (bankAfterDebit == null || bankAfterDebit.getPointsAvailable() == null) {
      throw new InsufficientBalanceException("Unable to debit points");
    }
    walletService.setBalance(pointsWallet.getId(), bankAfterDebit.getPointsAvailable());

    Topup topup = new Topup();
    topup.setUserId(userId);
    topup.setPointsDebited(pointsDebited);
    topup.setMgcCredited(mgcCredited);
    topup.setRatePointsPerMgcSnapshot(pointsPerMgc);
    topup.setRateId(rate.getId());
    topup.setStatus(TopupStatus.COMPLETED);
    topup.setIdempotencyKey(idempotencyKey);

    Topup saved = topupRepository.save(topup);

    walletService.updateBalance(mgcWallet.getId(), mgcCredited, LedgerDirection.CREDIT);

    ledgerService.createEntry(
      LedgerRefType.TOPUP,
      saved.getId(),
      pointsWallet.getId(),
      LedgerDirection.DEBIT,
      AssetType.POINTS,
      pointsDebited
    );

    ledgerService.createEntry(
      LedgerRefType.TOPUP,
      saved.getId(),
      mgcWallet.getId(),
      LedgerDirection.CREDIT,
      AssetType.MGC,
      mgcCredited
    );

    idempotencyService.storeResult(redisKey, saved.getId().toString());
    return saved;
  }

  public List<Topup> listTopups(UUID userId) {
    return topupRepository.findByUserIdOrderByCreatedAtDesc(userId);
  }
}
