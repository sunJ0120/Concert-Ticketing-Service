package com.example.be.auth;

import com.example.be.auth.dto.LoginRequest;
import com.example.be.auth.dto.LoginResponse;
import com.example.be.auth.dto.SignupRequest;
import com.example.be.auth.service.RedisTokenService;
import com.example.be.security.JwtProvider;
import com.example.be.user.Role;
import com.example.be.user.User;
import com.example.be.user.UserRepository;
import java.net.http.HttpRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final JwtProvider jwtProvider;
  private final PasswordEncoder passwordEncoder;
  private final RedisTokenService redisTokenService;

  public void signup(SignupRequest request) {
    if (userRepository.findByEmail(request.email())
        .isPresent()) {
      throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
    }

    String encodedPassword = passwordEncoder.encode(request.password());

    User user = User.builder()
        .email(request.email())
        .password(encodedPassword)
        .name(request.name())
        .phone(request.phone())
        .role(Role.USER)
        .build();

    userRepository.save(user);
  }

  public LoginResponse login(LoginRequest request) {
    User user = userRepository.findByEmail(request.email())
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }

    String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getRole());
    String refreshToken = jwtProvider.generateRefreshToken(user.getId(), user.getRole());

    long expirationMillis = jwtProvider.getExpiration(refreshToken) - System.currentTimeMillis();
    redisTokenService.addToWhitelist(user.getId(), refreshToken, expirationMillis);

    return new LoginResponse(accessToken, refreshToken);
  }

  public void logout(String token) {
    long expirationMillis = jwtProvider.getExpiration(token) - System.currentTimeMillis();
    redisTokenService.addToBlacklist(token, expirationMillis);

    Long userId = jwtProvider.getUserId(token);
    redisTokenService.deleteRefreshToken(userId);

    SecurityContextHolder.clearContext();
  }

  public LoginResponse refresh(String refreshToken) {
    try {
      jwtProvider.validateToken(refreshToken);
    } catch (io.jsonwebtoken.JwtException e) {
      return null;
    }

    Long userId = jwtProvider.getUserId(refreshToken);
    Role role = jwtProvider.getRole(refreshToken);
    if (redisTokenService.isValidRefreshToken(userId, refreshToken)) {
      // 4. refresh로 accesstoken 갱신
      String newAccessToken = jwtProvider.generateAccessToken(userId, role);
      // 5. 다시 전송
      return new LoginResponse(newAccessToken, null);
    }
    return null;
  }
}
