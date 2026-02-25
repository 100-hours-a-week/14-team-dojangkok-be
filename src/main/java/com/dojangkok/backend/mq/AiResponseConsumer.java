package com.dojangkok.backend.mq;

import com.dojangkok.backend.mq.config.RabbitMQConfig;
import com.dojangkok.backend.mq.dto.AiResponseDto;
import com.dojangkok.backend.mq.dto.ChecklistMqResponseDto;
import com.dojangkok.backend.mq.dto.EasyContractMqResponseDto;
import com.dojangkok.backend.service.ChecklistTemplateService;
import com.dojangkok.backend.service.EasyContractService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiResponseConsumer {

    private final EasyContractService easyContractService;
    private final ChecklistTemplateService checklistTemplateService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String REDIS_KEY_PREFIX = "sse:events:";

    @RabbitListener(queues = RabbitMQConfig.AI_RESPONSE_QUEUE)
    public void handleResponse(AiResponseDto message) {
        log.info("MQ response consumed: type={}, correlationId={}, success={}",
                message.getType(), message.getCorrelationId(), message.isSuccess());

        if (message instanceof EasyContractMqResponseDto easyContractResponse) {
            handleEasyContractResponse(easyContractResponse);
        } else if (message instanceof ChecklistMqResponseDto checklistResponse) {
            handleChecklistResponse(checklistResponse);
        } else {
            log.warn("Unknown message type: {}", message.getType());
        }
    }

    private void handleEasyContractResponse(EasyContractMqResponseDto response) {
        // 1) Service에 위임 (DB 업데이트)
        easyContractService.completeCreateEasyContract(response);

        // 2) Redis List에 push (SSE push용)
        pushToRedis(response.getMemberId(), response);

        log.info("EasyContract response processed: easyContractId={}", response.getEasyContractId());
    }

    private void handleChecklistResponse(ChecklistMqResponseDto response) {
        // 1) Service에 위임 (DB 업데이트)
        if (response.isSuccess()) {
            checklistTemplateService.completeChecklistGeneration(
                    response.getTemplateId(), response.getChecklists());
        } else {
            checklistTemplateService.completeChecklistGeneration(
                    response.getTemplateId(), null);
        }

        // 2) Redis List에 push (SSE push용)
        pushToRedis(response.getMemberId(), response);

        log.info("Checklist response processed: templateId={}", response.getTemplateId());
    }

    private void pushToRedis(Long memberId, AiResponseDto response) {
        if (memberId == null) return;

        try {
            String key = REDIS_KEY_PREFIX + memberId;
            String value = objectMapper.writeValueAsString(response);
            redisTemplate.opsForList().rightPush(key, value);
            redisTemplate.expire(key, Duration.ofMinutes(5));
        } catch (Exception e) {
            log.error("Failed to save to Redis: correlationId={}", response.getCorrelationId(), e);
        }
    }
}
