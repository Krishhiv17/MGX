package com.mgx.otp.controller;

import com.mgx.otp.dto.OtpRequest;
import com.mgx.otp.dto.OtpResponse;
import com.mgx.otp.dto.OtpVerifyRequest;
import com.mgx.otp.dto.OtpVerifyResponse;
import com.mgx.otp.service.OtpService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/otp")
public class OtpController {
  private final OtpService otpService;

  public OtpController(OtpService otpService) {
    this.otpService = otpService;
  }

  @PostMapping("/request")
  public OtpResponse request(@RequestBody OtpRequest request) {
    return otpService.requestOtp(request.getPhoneNumber(), request.getPurpose());
  }

  @PostMapping("/verify")
  public OtpVerifyResponse verify(@RequestBody OtpVerifyRequest request) {
    return otpService.verifyOtp(request.getSessionId(), request.getCode());
  }
}
