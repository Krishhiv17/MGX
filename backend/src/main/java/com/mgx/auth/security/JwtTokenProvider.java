package com.mgx.auth.security;

import com.mgx.user.model.User;
import com.mgx.user.model.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
  private final Key signingKey;
  private final long expirationMs;

  public JwtTokenProvider(
    @Value("${mgx.jwt.secret}") String secret,
    @Value("${mgx.jwt.expiration-ms:3600000}") long expirationMs
  ) {
    this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expirationMs = expirationMs;
  }

  public String generateToken(User user) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + expirationMs);

    return Jwts.builder()
      .setSubject(user.getId().toString())
      .claim("email", user.getEmail())
      .claim("role", user.getRole().name())
      .setIssuedAt(now)
      .setExpiration(expiry)
      .signWith(signingKey, SignatureAlgorithm.HS256)
      .compact();
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token);
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  public JwtUserPrincipal getPrincipal(String token) {
    Claims claims = Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token).getBody();
    UUID userId = UUID.fromString(claims.getSubject());
    String email = claims.get("email", String.class);
    UserRole role = UserRole.valueOf(claims.get("role", String.class));
    return new JwtUserPrincipal(userId, email, role);
  }
}
