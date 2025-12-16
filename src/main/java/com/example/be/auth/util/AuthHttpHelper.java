package com.example.be.auth.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class AuthHttpHelper {

  private static final String REFRESH_TOKEN_NAME = "refresh_token";

  public String extractRefreshToken(HttpServletRequest request) {
    // 1. request에서 header 분리
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }

    for (Cookie cookie : request.getCookies()) {
      if (REFRESH_TOKEN_NAME.equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    return null;
  }

  public static ResponseCookie createLogoutCookie() {
    ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_NAME, "")
        .maxAge(0)
        .path("/api/auth/refresh")
        .httpOnly(true)
        .build();
    return cookie;
  }

  public ResponseCookie getResponseCookie(String refreshToken) {
    ResponseCookie responseCookie = ResponseCookie.from(REFRESH_TOKEN_NAME, refreshToken)
        .maxAge(3600)
        .path("/api/auth/refresh")    // 해당 url이 요청할때만 전송
        .secure(true)
        .httpOnly(true)
        .sameSite("Lax")
        .build();
    return responseCookie;
  }
}
