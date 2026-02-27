package com.dojangkok.backend.common.config;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;

@Slf4j
@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    @RefreshScope
    public DataSource dataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void warmUpDataSource(ApplicationReadyEvent event) {
        try {
            DataSource ds = event.getApplicationContext().getBean(DataSource.class);
            ds.getConnection().close();
            log.info("DataSource warmup completed");
        } catch (Exception e) {
            log.warn("DataSource warmup failed: {}", e.getMessage());
        }
    }
}
