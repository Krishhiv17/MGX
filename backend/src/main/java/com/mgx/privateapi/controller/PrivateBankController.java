package com.mgx.privateapi.controller;

import com.mgx.banklink.model.BankLink;
import com.mgx.banklink.repository.BankLinkRepository;
import com.mgx.game.dto.GameResponse;
import com.mgx.game.model.Game;
import com.mgx.game.model.GameStatus;
import com.mgx.game.repository.GameRepository;
import com.mgx.game.service.GameCountryService;
import com.mgx.otp.model.OtpPurpose;
import com.mgx.otp.model.OtpSession;
import com.mgx.otp.service.OtpService;
import com.mgx.privateapi.dto.PrivatePurchaseRequest;
import com.mgx.purchase.dto.PurchaseResponse;
import com.mgx.purchase.model.Purchase;
import com.mgx.purchase.service.PurchaseService;
import com.mgx.rates.dto.MgcUgcRateResponse;
import com.mgx.rates.dto.PointsMgcRateResponse;
import com.mgx.rates.model.RateMgcUgc;
import com.mgx.rates.model.RatePointsMgc;
import com.mgx.rates.service.RateService;
import com.mgx.supportedcountry.model.SupportedCountry;
import com.mgx.supportedcountry.repository.SupportedCountryRepository;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/private")
public class PrivateBankController {
  private final GameRepository gameRepository;
  private final GameCountryService gameCountryService;
  private final SupportedCountryRepository supportedCountryRepository;
  private final RateService rateService;
  private final OtpService otpService;
  private final BankLinkRepository bankLinkRepository;
  private final PurchaseService purchaseService;

  public PrivateBankController(
    GameRepository gameRepository,
    GameCountryService gameCountryService,
    SupportedCountryRepository supportedCountryRepository,
    RateService rateService,
    OtpService otpService,
    BankLinkRepository bankLinkRepository,
    PurchaseService purchaseService
  ) {
    this.gameRepository = gameRepository;
    this.gameCountryService = gameCountryService;
    this.supportedCountryRepository = supportedCountryRepository;
    this.rateService = rateService;
    this.otpService = otpService;
    this.bankLinkRepository = bankLinkRepository;
    this.purchaseService = purchaseService;
  }

  @GetMapping("/games")
  public List<GameResponse> listGames(@RequestParam("country") String country) {
    String countryCode = normalizeCountry(country);
    requireActiveCountry(countryCode);

    List<Game> games = gameRepository.findByStatusAndCountryCode(
      GameStatus.ACTIVE.name(),
      countryCode
    );
    Map<UUID, List<String>> allowedMap = gameCountryService.getAllowedCountriesForGames(
      games.stream().map(Game::getId).collect(Collectors.toList())
    );
    return games.stream()
      .map(game -> GameResponse.from(game, allowedMap.get(game.getId())))
      .collect(Collectors.toList());
  }

  @GetMapping("/rates")
  public MgcUgcRateResponse getRates(
    @RequestParam("gameId") UUID gameId,
    @RequestParam(value = "country", required = false) String country
  ) {
    if (gameId == null) {
      throw new IllegalArgumentException("gameId is required");
    }
    if (country != null && !country.isBlank()) {
      String countryCode = normalizeCountry(country);
      requireActiveCountry(countryCode);
      if (!gameCountryService.isAllowed(gameId, countryCode)) {
        throw new IllegalArgumentException("Game not available in requested country");
      }
    }
    RateMgcUgc rate = rateService.getActiveMgcToUgcRate(gameId);
    return MgcUgcRateResponse.from(rate);
  }

  @GetMapping("/points-mgc-rate")
  public PointsMgcRateResponse getPointsMgcRate() {
    RatePointsMgc rate = rateService.getActivePointsToMgcRate();
    return PointsMgcRateResponse.from(rate);
  }

  @PostMapping("/purchase")
  public PurchaseResponse purchase(
    @RequestHeader("Idempotency-Key") String idempotencyKey,
    @RequestBody PrivatePurchaseRequest request
  ) {
    if (request.getGameId() == null) {
      throw new IllegalArgumentException("gameId is required");
    }
    if (request.getPhoneNumber() == null || request.getPhoneNumber().isBlank()) {
      throw new IllegalArgumentException("phoneNumber is required");
    }
    if (request.getBankRef() == null || request.getBankRef().isBlank()) {
      throw new IllegalArgumentException("bankRef is required");
    }
    if (request.getOtpSessionId() == null) {
      throw new IllegalArgumentException("otpSessionId is required");
    }

    OtpSession session = otpService.getVerifiedSession(request.getOtpSessionId());
    if (session.getPurpose() != OtpPurpose.BANK_PURCHASE) {
      throw new IllegalArgumentException("OTP purpose mismatch");
    }
    if (!session.getPhoneNumber().equals(request.getPhoneNumber())) {
      throw new IllegalArgumentException("OTP phone mismatch");
    }

    BankLink link = bankLinkRepository.findByPhoneNumberAndBankRef(
      request.getPhoneNumber(),
      request.getBankRef()
    ).orElseThrow(() -> new IllegalArgumentException("Bank link not found"));
    if (link.getVerifiedAt() == null) {
      throw new IllegalArgumentException("Bank link not verified");
    }

    Purchase purchase = purchaseService.createPurchase(
      link.getUserId(),
      request.getGameId(),
      request.getMgcAmount(),
      request.getUgcAmount(),
      idempotencyKey
    );
    return PurchaseResponse.from(purchase);
  }

  private String normalizeCountry(String country) {
    if (country == null || country.isBlank()) {
      throw new IllegalArgumentException("country is required");
    }
    return country.trim().toUpperCase(Locale.ROOT);
  }

  private void requireActiveCountry(String countryCode) {
    SupportedCountry country = supportedCountryRepository.findById(countryCode)
      .orElseThrow(() -> new IllegalArgumentException("Country is not supported"));
    if (!"ACTIVE".equalsIgnoreCase(country.getStatus())) {
      throw new IllegalArgumentException("Country is not supported");
    }
  }
}
