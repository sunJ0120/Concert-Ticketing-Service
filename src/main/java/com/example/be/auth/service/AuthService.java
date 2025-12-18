package com.example.be.auth.service;

import com.example.be.auth.dto.LoginRequest;
import com.example.be.auth.dto.LoginResponse;
import com.example.be.auth.dto.SignupRequest;
import com.example.be.auth.validator.AuthValidator;
import com.example.be.security.JwtProvider;
import com.example.be.user.Provider;
import com.example.be.user.Role;
import com.example.be.user.SocialAccount;
import com.example.be.user.SocialAccountRepository;
import com.example.be.user.User;
import com.example.be.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final SocialAccountRepository socialAccountRepository;
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

    return createTokens(user);
  }

  @Transactional
  public LoginResponse socialLogin(String provider, String providerId, String email, String name) {
    User user = userRepository.findByEmail(email)
        .orElseGet(() -> {
          User newUser = User.builder()
              .email(email)
              .password(null)
              .name(name)
              .role(Role.USER)
              .build();

          return userRepository.save(newUser);
        });

    Provider providerEnum = Provider.valueOf(provider.toUpperCase());
    socialAccountRepository.findByProviderAndProviderId(providerEnum,
            providerId)
        .orElseGet(() -> {
          SocialAccount newSocialAccount = SocialAccount.builder()
              .provider(providerEnum)
              .providerId(providerId)
              .user(user)
              .build();

          return socialAccountRepository.save(newSocialAccount);
        });

    return createTokens(user);
  }

  private LoginResponse createTokens(User user) {
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
    Long userId = jwtProvider.getUserId(refreshToken);
    Role role = jwtProvider.getRole(refreshToken);

    if (!redisTokenService.isValidRefreshToken(userId, refreshToken)) {    // Redis 서버측 검증
      throw new BadCredentialsException(
          "Refresh Token이 유효하지 않거나 탈취되었습니다.");    // 서버 측 검증 실패 시 구체적인 예외
    }

    String newAccessToken = jwtProvider.generateAccessToken(userId, role);
    return new LoginResponse(newAccessToken, null);
  }
}
