package com.dojangkok.backend.auth.token;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis 기반 Refresh Token 저장소
 * - Rotation 방식: 토큰 재발급 시 기존 토큰 무효화
 * - TTL 자동 만료
 */
@Component
@RequiredArgsConstructor
public class RedisRefreshTokenStore {

    private final StringRedisTemplate redisTemplate;
    
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:member:";
    private static final String TOKEN_TO_MEMBER_PREFIX = "refresh_token:token:";

    public void save(Long memberId, String refreshToken, Duration ttl) {
        String memberKey = REFRESH_TOKEN_PREFIX + memberId;
        String tokenKey = TOKEN_TO_MEMBER_PREFIX + refreshToken;
        
        // 기존 토큰이 있으면 삭제 (Rotation)
        String oldToken = redisTemplate.opsForValue().get(memberKey);
        if (oldToken != null) {
            redisTemplate.delete(TOKEN_TO_MEMBER_PREFIX + oldToken);
        }
        
        // 새 토큰 저장
        redisTemplate.opsForValue().set(memberKey, refreshToken, ttl);
        redisTemplate.opsForValue().set(tokenKey, String.valueOf(memberId), ttl);
    }

    public void save(Long memberId, String refreshToken) {
        save(memberId, refreshToken, Duration.ofDays(14));
    }

    public Optional<String> find(Long memberId) {
        String key = REFRESH_TOKEN_PREFIX + memberId;
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    public boolean validate(Long memberId, String refreshToken) {
        return find(memberId)
                .map(storedToken -> storedToken.equals(refreshToken))
                .orElse(false);
    }

    public void delete(Long memberId) {
        String memberKey = REFRESH_TOKEN_PREFIX + memberId;
        String token = redisTemplate.opsForValue().get(memberKey);
        
        if (token != null) {
            redisTemplate.delete(TOKEN_TO_MEMBER_PREFIX + token);
        }
        redisTemplate.delete(memberKey);
    }

    public void deleteByToken(String refreshToken) {
        String tokenKey = TOKEN_TO_MEMBER_PREFIX + refreshToken;
        String memberId = redisTemplate.opsForValue().get(tokenKey);
        
        if (memberId != null) {
            redisTemplate.delete(REFRESH_TOKEN_PREFIX + memberId);
        }
        redisTemplate.delete(tokenKey);
    }
}
