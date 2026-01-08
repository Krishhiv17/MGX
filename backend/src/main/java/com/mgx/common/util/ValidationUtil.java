package com.mgx.common.util;

import java.math.BigDecimal;
import java.util.Set;

public final class ValidationUtil {
  private static final Set<String> SUPPORTED_CURRENCIES = Set.of("USD", "EUR", "GBP", "INR", "JPY");

  private ValidationUtil() {}

  public static void requirePositive(BigDecimal amount, String fieldName) {
    if (amount == null || amount.signum() <= 0) {
      throw new IllegalArgumentException(fieldName + " must be positive");
    }
  }

  public static void requireCurrency(String currency) {
    if (currency == null || !SUPPORTED_CURRENCIES.contains(currency)) {
      throw new IllegalArgumentException("Unsupported currency");
    }
  }

  public static void requireNonBlank(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " is required");
    }
  }
}
