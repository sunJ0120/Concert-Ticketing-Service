package com.example.be.security;

import com.example.be.user.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {

  private final SecretKey secretKey;
  private final long accessTokenExpiration;
  private final long refreshTokenExpiration;

  public JwtProvider(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
      @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.accessTokenExpiration = accessTokenExpiration;
    this.refreshTokenExpiration = refreshTokenExpiration;
  }

  public String generateAccessToken(Long userId, Role role) {
    return Jwts.builder()
        .subject(String.valueOf(userId))
        .claim("type", "access")
        .claim("role", role.getRole())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
        .signWith(secretKey)
        .compact();
  }

  public String generateRefreshToken(Long userId) {
    return Jwts.builder()
        .subject(String.valueOf(userId))
        .claim("type", "refresh")
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
        .signWith(secretKey)
        .compact();
  }

  public boolean validateToken(String token) {
    if (token == null || token.isBlank()) {
      return false;
    }

    try {
      Jwts.parser()
          .verifyWith(secretKey)
          .build()
          .parseSignedClaims(token);
    } catch (JwtException e) {
      return false;
    }
    return true;
  }

  public Long getUserId(String token) {
    Claims claims = getClaims(token);

    return Long.parseLong(claims.getSubject());
  }

  public String getRole(String token) {
    Claims claims = getClaims(token);

    return claims.get("role", String.class);
  }

  private Claims getClaims(String token) {
    return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public Long getExpiration(String token) {
    Claims claims = getClaims(token);

    return claims.getExpiration()
        .getTime();
  }

  public Authentication getAuthentication(String token) {
    Long userId = getUserId(token);
    String role = getRole(token);

    return new UsernamePasswordAuthenticationToken(
        userId,
        null,
        List.of(new SimpleGrantedAuthority("ROLE_" + role))
    );
  }
}
