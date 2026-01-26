package com.dojangkok.backend.auth.token;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis 기반 교환 코드 저장소
 * - 일회성 코드로 토큰 교환
 * - 짧은 TTL (30초)로 보안 강화
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisExchangeCodeStore {

    private final StringRedisTemplate redisTemplate;
    private final JsonMapper jsonMapper;
    
    private static final String EXCHANGE_CODE_PREFIX = "exchange_code:";
    private static final Duration TTL = Duration.ofSeconds(30);

    public void save(String code, ExchangeData data) {
        String key = EXCHANGE_CODE_PREFIX + code;
        try {
            String json = jsonMapper.writeValueAsString(data);
            redisTemplate.opsForValue().set(key, json, TTL);
            log.info("Redis에 교환 코드 저장 완료 - key: {}, ttl: {}초", key, TTL.getSeconds());
        } catch (JacksonException e) {
            log.error("Failed to serialize exchange data", e);
            throw new IllegalStateException("Failed to save exchange code", e);
        }
    }

    public Optional<ExchangeData> consume(String code) {
        String key = EXCHANGE_CODE_PREFIX + code;
        String json = redisTemplate.opsForValue().getAndDelete(key);
        
        if (json == null) {
            return Optional.empty();
        }
        
        try {
            ExchangeData data = jsonMapper.readValue(json, ExchangeData.class);
            return Optional.of(data);
        } catch (JacksonException e) {
            log.error("Failed to deserialize exchange data", e);
            return Optional.empty();
        }
    }

    public record ExchangeData(
            Long memberId,
            boolean isNewUser
    ) {}
}
