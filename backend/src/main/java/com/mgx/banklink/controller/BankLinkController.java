package com.mgx.banklink.controller;

import com.mgx.auth.security.JwtUserPrincipal;
import com.mgx.banklink.dto.BankLinkRequest;
import com.mgx.banklink.dto.BankLinkResponse;
import com.mgx.banklink.model.BankLink;
import com.mgx.banklink.service.BankLinkService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/bank-links")
public class BankLinkController {
  private final BankLinkService bankLinkService;

  public BankLinkController(BankLinkService bankLinkService) {
    this.bankLinkService = bankLinkService;
  }

  @PostMapping
  @PreAuthorize("isAuthenticated()")
  public BankLinkResponse link(
    @AuthenticationPrincipal JwtUserPrincipal principal,
    @RequestBody BankLinkRequest request
  ) {
    if (principal == null) {
      throw new IllegalArgumentException("Access denied");
    }
    BankLink link = bankLinkService.linkBank(
      principal.getUserId(),
      request.getBankRef(),
      request.getPhoneNumber(),
      request.getOtpSessionId()
    );
    return BankLinkResponse.from(link);
  }
}
