package com.dojangkok.backend.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.fasterxml.jackson.databind.json.JsonMapper;

@Slf4j
@Configuration
@Profile("!test")
public class RedisConfig {

    @Bean
    @RefreshScope
    public RedisConnectionFactory redisConnectionFactory(RedisProperties redisProperties) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisProperties.getHost());
        config.setPort(redisProperties.getPort());
        return new LettuceConnectionFactory(config);
    }

    @Bean
    @RefreshScope
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    @Bean
    public JsonMapper jsonMapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .build();
    }

    /**
     * RefreshScope 빈은 lazy proxy이므로, 앱 시작 직후 강제로 커넥션을 초기화하여
     * 배포 시 첫 요청에서 503이 발생하는 것을 방지
     */
    @EventListener(ApplicationReadyEvent.class)
    public void warmUpRedis(ApplicationReadyEvent event) {
        try {
            StringRedisTemplate template = event.getApplicationContext().getBean(StringRedisTemplate.class);
            template.getConnectionFactory().getConnection().ping();
            log.info("Redis warmup completed");
        } catch (Exception e) {
            log.warn("Redis warmup failed: {}", e.getMessage());
        }
    }
}
