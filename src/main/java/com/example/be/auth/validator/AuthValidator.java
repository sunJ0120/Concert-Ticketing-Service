package com.example.be.auth.validator;

import com.example.be.security.jwt.JwtProvider;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthValidator {

  private final JwtProvider jwtProvider;

  public String validateAndGetToken(String token) throws JwtException {
    if (token == null || token.isBlank()) {
      throw new IllegalArgumentException("토큰이 존재하지 않거나 빈 문자열입니다.");
    }

    jwtProvider.validateToken(token);

    return token;
  }
}
