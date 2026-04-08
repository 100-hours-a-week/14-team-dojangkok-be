package com.dojangkok.backend.service;

import com.dojangkok.backend.domain.OutboxEvent;
import com.dojangkok.backend.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;


    public OutboxEvent saveEvent(String aggregateType, Long aggregateId,
                                 String exchange, String routingKey, Object payload) {
        String payloadJson = serializePayload(payload);
        OutboxEvent event = OutboxEvent.create(aggregateType, aggregateId, exchange, routingKey, payloadJson);
        return outboxEventRepository.save(event);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishAndMark(Long outboxEventId) {
        OutboxEvent event = outboxEventRepository.findById(outboxEventId)
                .orElse(null);

        if (event == null || event.getPublishedAt() != null) {
            return;
        }

        publishToMq(event);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void retryPublish(OutboxEvent event) {
        // PENDING 상태의 이벤트를 재발행 시도
        publishToMq(event);
    }

    private void publishToMq(OutboxEvent event) {
        try {
            Object payload = objectMapper.readValue(event.getPayload(), Map.class);
            rabbitTemplate.convertAndSend(event.getExchange(), event.getRoutingKey(), payload);
            event.markPublished();
            log.info("Outbox event published: id={}, aggregateType={}, aggregateId={}",
                    event.getId(), event.getAggregateType(), event.getAggregateId());
        } catch (Exception e) {
            event.incrementRetryCount();
            if (!event.isRetryable()) {
                event.markFailed();
                log.error("Outbox event exhausted retries: id={}", event.getId(), e);
            } else {
                log.warn("Outbox event publish failed, will retry: id={}, retryCount={}",
                        event.getId(), event.getRetryCount(), e);
            }
        }
        outboxEventRepository.save(event);
    }

    private String serializePayload(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize outbox payload", e);
        }
    }
}
