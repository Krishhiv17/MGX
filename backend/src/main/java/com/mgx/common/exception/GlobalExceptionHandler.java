package com.mgx.common.exception;

import com.mgx.common.dto.ApiError;
import java.time.OffsetDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
    ApiError error = new ApiError("Validation failed", "VALIDATION_ERROR", OffsetDateTime.now());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiError> handleAuth(AuthenticationException ex) {
    ApiError error = new ApiError("Authentication failed", "AUTH_ERROR", OffsetDateTime.now());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiError> handleAccess(AccessDeniedException ex) {
    ApiError error = new ApiError("Access denied", "FORBIDDEN", OffsetDateTime.now());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
  }

  @ExceptionHandler({
    InsufficientBalanceException.class,
    IdempotencyConflictException.class,
    RateNotFoundException.class,
    WalletNotFoundException.class,
    GameNotFoundException.class,
    FxWindowNotFoundException.class,
    ReceivableNotFoundException.class,
    IllegalArgumentException.class
  })
  public ResponseEntity<ApiError> handleBadRequest(RuntimeException ex) {
    ApiError error = new ApiError(ex.getMessage(), "BUSINESS_ERROR", OffsetDateTime.now());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleServer(Exception ex) {
    ApiError error = new ApiError("Internal server error", "SERVER_ERROR", OffsetDateTime.now());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }
}
