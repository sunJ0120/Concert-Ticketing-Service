package com.example.be.security;

import com.example.be.user.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String AUTHORIZATION_HEADER_PREFIX = "Bearer ";

  private final JwtProvider jwtProvider;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String authHeader = request.getHeader(
        AUTHORIZATION_HEADER);    // 1. Authorization header 찾아서 Bearer 뒤에 있는 토큰 찾기
    if (authHeader != null && authHeader.startsWith(AUTHORIZATION_HEADER_PREFIX)) {
      String token = authHeader.substring(AUTHORIZATION_HEADER_PREFIX.length());
      if (jwtProvider.validateToken(token)) {    // 2. token 유효성을 jwtProvider로 검사
        Long userId = jwtProvider.getUserId(token);    // 3. Context에 인증 유저 정보 저장
        String role = jwtProvider.getRole(token);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null,
            List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
      }
    }
    filterChain.doFilter(request, response);
  }
}
