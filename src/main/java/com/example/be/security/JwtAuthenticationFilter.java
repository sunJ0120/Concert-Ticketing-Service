package com.example.be.security;

import com.example.be.auth.service.RedisTokenService;
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
  private final RedisTokenService redisTokenService;
  private final JwtUtils jwtUtils;

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();

    return path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.startsWith(
        "/swagger-resources") || path.startsWith("/h2-console") || path.startsWith("/auth");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    // @formatter:off
    String token = jwtUtils.resolveToken(request);

    if (token == null || token.isBlank()) {
      filterChain.doFilter(request, response);
    }

    try {
      jwtProvider.validateToken(token);
    } catch (io.jsonwebtoken.JwtException e) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().println(e.getMessage());

      return;    // 필터 중단
    }

    if(redisTokenService.isBlacklisted(token)){
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    Authentication authentication = jwtProvider.getAuthentication(token);
    SecurityContextHolder.getContext().setAuthentication(authentication);

    filterChain.doFilter(request, response);
  }
}
