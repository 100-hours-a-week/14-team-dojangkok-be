package com.dojangkok.backend.config;

import com.dojangkok.backend.auth.token.RedisExchangeCodeStore;
import com.dojangkok.backend.auth.token.RedisRefreshTokenStore;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 테스트 환경을 위한 설정 클래스
 * Redis를 Mock으로 대체하여 Docker 없이도 테스트 가능
 */
@TestConfiguration
@Profile("test")
public class TestConfig {

    private final Map<String, String> mockRedisStorage = new ConcurrentHashMap<>();

    @Bean
    @Primary
    public StringRedisTemplate stringRedisTemplate() {
        StringRedisTemplate mockTemplate = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);

        when(mockTemplate.opsForValue()).thenReturn(valueOps);

        // set 메서드
        doAnswer(invocation -> {
            String key = invocation.getArgument(0);
            String value = invocation.getArgument(1);
            mockRedisStorage.put(key, value);
            return null;
        }).when(valueOps).set(anyString(), anyString());

        doAnswer(invocation -> {
            String key = invocation.getArgument(0);
            String value = invocation.getArgument(1);
            mockRedisStorage.put(key, value);
            return null;
        }).when(valueOps).set(anyString(), anyString(), any(Duration.class));

        // get 메서드
        when(valueOps.get(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            return mockRedisStorage.get(key);
        });

        // getAndDelete 메서드
        when(valueOps.getAndDelete(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            return mockRedisStorage.remove(key);
        });

        // delete 메서드
        when(mockTemplate.delete(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            return mockRedisStorage.remove(key) != null;
        });

        return mockTemplate;
    }

    @Bean
    @Primary
    public JsonMapper jsonMapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .build();
    }

    @Bean
    @Primary
    public RedisRefreshTokenStore redisRefreshTokenStore(StringRedisTemplate stringRedisTemplate) {
        return new RedisRefreshTokenStore(stringRedisTemplate);
    }

    @Bean
    @Primary
    public RedisExchangeCodeStore redisExchangeCodeStore(StringRedisTemplate stringRedisTemplate, JsonMapper jsonMapper) {
        return new RedisExchangeCodeStore(stringRedisTemplate, jsonMapper);
    }
}
