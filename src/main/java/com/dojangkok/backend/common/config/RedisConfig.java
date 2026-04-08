package com.dojangkok.backend.common.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.SocketOptions.KeepAliveOptions;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Bean
    public LettuceClientConfigurationBuilderCustomizer lettuceCustomizer() {
        return builder -> builder.clientOptions(
                ClientOptions.builder()
                        .socketOptions(SocketOptions.builder()
                                .keepAlive(KeepAliveOptions.builder()
                                        .enable()
                                        .idle(Duration.ofSeconds(30))
                                        .interval(Duration.ofSeconds(10))
                                        .count(3)
                                        .build())
                                .build())
                        .build()
        );
    }
}
