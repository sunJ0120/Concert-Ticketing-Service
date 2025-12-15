package com.example.be.security;

import com.example.be.auth.service.RedisBlacklistService;
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

  private final JwtProvider jwtProvider;
  private final RedisBlacklistService redisBlacklistService;
  private final JwtUtils jwtUtils;

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();

    return path.startsWith("/swagger-ui")
        || path.startsWith("/v3/api-docs")
        || path.startsWith("/swagger-resources")
        || path.startsWith("/h2-console")
        || path.startsWith("/auth");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    // @formatter:off
    String token = jwtUtils.resolveToken(request);

    if (token == null || token.isBlank()) {
      filterChain.doFilter(request, response);
      return;
    }

    if(redisBlacklistService.isBlacklisted(token)){
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    if (jwtProvider.validateToken(token)) {    // 2. token 유효성을 jwtProvider로 검사
      Authentication authentication = jwtProvider.getAuthentication(token);
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    filterChain.doFilter(request, response);
  }
}
