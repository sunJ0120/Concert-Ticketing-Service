package com.example.be.security;

import com.example.be.auth.service.RedisBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String AUTHORIZATION_HEADER_PREFIX = "Bearer ";

  public String resolveToken(HttpServletRequest request) {
    String authHeader = request.getHeader(
        AUTHORIZATION_HEADER);    // 1. Authorization header 찾아서 Bearer 뒤에 있는 토큰 찾기
    if (authHeader != null && authHeader.startsWith(AUTHORIZATION_HEADER_PREFIX)) {
      return authHeader.substring(AUTHORIZATION_HEADER_PREFIX.length());
    }
    return null;
  }
}
