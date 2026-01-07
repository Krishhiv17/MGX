package com.mgx.apikey.security;

import com.mgx.apikey.model.ApiKey;
import com.mgx.apikey.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
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
      ApiKey apiKey = apiKeyService.findActiveByRawKey(rawKey).orElse(null);
      if (apiKey == null) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }
      List<SimpleGrantedAuthority> authorities = buildAuthorities(apiKey.getScopes());
      UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
        "api-key",
        null,
        authorities
      );
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(request, response);
  }

  private List<SimpleGrantedAuthority> buildAuthorities(String scopes) {
    if (scopes == null || scopes.isBlank()) {
      return List.of(new SimpleGrantedAuthority("API_KEY"));
    }
    List<SimpleGrantedAuthority> scoped = Arrays.stream(scopes.split(","))
      .map(String::trim)
      .filter(scope -> !scope.isBlank())
      .map(scope -> "SCOPE_" + scope.toUpperCase(Locale.ROOT))
      .map(SimpleGrantedAuthority::new)
      .collect(Collectors.toList());
    scoped.add(new SimpleGrantedAuthority("API_KEY"));
    return scoped;
  }
}
