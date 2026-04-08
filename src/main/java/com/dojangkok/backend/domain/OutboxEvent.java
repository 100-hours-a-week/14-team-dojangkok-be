package com.dojangkok.backend.domain;

import com.dojangkok.backend.common.entity.BaseCreatedTimeEntity;
import com.dojangkok.backend.domain.enums.OutboxStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "outbox_event",
        indexes = {
                @Index(name = "idx_outbox_status_created_at", columnList = "status, created_at")
        }
)
public class OutboxEvent extends BaseCreatedTimeEntity {

    private static final int MAX_RETRY_COUNT = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private Long aggregateId;

    @Column(name = "routing_key", nullable = false, length = 100)
    private String routingKey;

    @Column(name = "exchange", nullable = false, length = 100)
    private String exchange;

    @Lob
    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OutboxStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private OutboxEvent(String aggregateType, Long aggregateId, String routingKey,
                        String exchange, String payload, OutboxStatus status, int retryCount) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.routingKey = routingKey;
        this.exchange = exchange;
        this.payload = payload;
        this.status = status;
        this.retryCount = retryCount;
    }

    public static OutboxEvent create(String aggregateType, Long aggregateId,
                                     String exchange, String routingKey, String payload) {
        return OutboxEvent.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .exchange(exchange)
                .routingKey(routingKey)
                .payload(payload)
                .status(OutboxStatus.PENDING)
                .retryCount(0)
                .build();
    }

    public void markPublished() {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public void markFailed() {
        this.status = OutboxStatus.FAILED;
    }

    public boolean isRetryable() {
        return this.retryCount < MAX_RETRY_COUNT;
    }
}
