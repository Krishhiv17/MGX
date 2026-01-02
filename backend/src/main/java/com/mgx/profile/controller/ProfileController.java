package com.mgx.profile.controller;

import com.mgx.auth.security.JwtUserPrincipal;
import com.mgx.profile.dto.ProfileResponse;
import com.mgx.profile.dto.UpdateProfileRequest;
import com.mgx.profile.service.ProfileService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/profile")
public class ProfileController {
  private final ProfileService profileService;

  public ProfileController(ProfileService profileService) {
    this.profileService = profileService;
  }

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public ProfileResponse getProfile(@AuthenticationPrincipal JwtUserPrincipal principal) {
    if (principal == null) {
      throw new AccessDeniedException("Access denied");
    }
    return profileService.getProfile(principal.getUserId());
  }

  @PutMapping
  @PreAuthorize("isAuthenticated()")
  public ProfileResponse updateProfile(
    @AuthenticationPrincipal JwtUserPrincipal principal,
    @RequestBody UpdateProfileRequest request
  ) {
    if (principal == null) {
      throw new AccessDeniedException("Access denied");
    }
    return profileService.updateCountry(principal.getUserId(), request.getCountryCode());
  }
}
