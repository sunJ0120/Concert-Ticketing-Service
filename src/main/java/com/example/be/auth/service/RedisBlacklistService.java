package com.example.be.auth.service;

import java.util.concurrent.TimeUnit;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisBlacklistService {

  private final RedisTemplate<String, Object> redisTemplate;
  private static final String BLACKLIST_PREFIX = "blacklist:";

  public String generateKey(String token) {
    return BLACKLIST_PREFIX + DigestUtils.sha256Hex(token);
  }

  public void addToBlacklist(String token, long expirationMillis) {
    String key = generateKey(token);

    redisTemplate.opsForValue()
        .set(
            key,
            "logout",
            expirationMillis,
            TimeUnit.MILLISECONDS
        );
  }

  public boolean isBlacklisted(@NonNull String token) {    // redis 블랙리스트 안에 존재여부를 따진다.
    String key = generateKey(token);
    return Boolean.TRUE.equals(redisTemplate.hasKey(key));
  }
}
