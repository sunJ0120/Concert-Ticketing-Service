package com.example.be.auth.service;

import java.util.concurrent.TimeUnit;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisTokenService {

  private final RedisTemplate<String, Object> redisTemplate;
  private static final String REFRESH_TOKEN_WHITELIST_PREFIX = "whitelist:rt:";
  private static final String ACCESS_TOKEN_BLACKLIST_PREFIX = "blacklist:at:";

  public String generateBlacklistKey(String token) {
    return ACCESS_TOKEN_BLACKLIST_PREFIX + DigestUtils.sha256Hex(token);
  }

  public String generateWhitelistKey(Long userId) {
    return REFRESH_TOKEN_WHITELIST_PREFIX + userId;
  }

  public void addToBlacklist(String token, long expirationMillis) {
    String key = generateBlacklistKey(token);

    redisTemplate.opsForValue()
        .set(
            key,
            "logout",
            expirationMillis,
            TimeUnit.MILLISECONDS
        );
  }

  public void addToWhitelist(Long userId, String refreshToken, long expirationMillis) {
    String key = generateWhitelistKey(userId);

    redisTemplate.opsForValue()
        .set(
            key,
            refreshToken,
            expirationMillis,
            TimeUnit.MILLISECONDS
        );
  }

  public boolean isBlacklisted(@NonNull String token) {    // redis 블랙리스트 안에 존재여부를 따진다.
    String key = generateBlacklistKey(token);
    return Boolean.TRUE.equals(redisTemplate.hasKey(key));
  }

  public boolean isValidRefreshToken(@NonNull Long userId, @NonNull String clientToken) {
    String key = generateWhitelistKey(userId);
    String storedToken = (String) redisTemplate.opsForValue()
        .get(key);

    return storedToken != null && storedToken.equals(clientToken);
  }

  public void deleteRefreshToken(@NonNull Long userId) {
    String key = generateWhitelistKey(userId);

    redisTemplate.delete(key);
  }
}
