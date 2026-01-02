package com.mgx.apikey.security;

import com.mgx.apikey.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {
  private final ApiKeyService apiKeyService;

  public ApiKeyAuthFilter(ApiKeyService apiKeyService) {
    this.apiKeyService = apiKeyService;
  }

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    String rawKey = request.getHeader("X-API-Key");
    if (rawKey != null && !rawKey.isBlank()) {
      if (apiKeyService.findActiveByRawKey(rawKey).isEmpty()) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }
      UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
        "api-key",
        null,
        List.of(new SimpleGrantedAuthority("API_KEY"))
      );
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(request, response);
  }
}
