package com.dojangkok.backend.mq;

import com.dojangkok.backend.mq.config.RabbitMQConfig;
import com.dojangkok.backend.mq.dto.ChecklistMqRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChecklistMqProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendRequest(ChecklistMqRequestDto request) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.WAS_EXCHANGE, "quorum.checklist.request", request);

        log.info("MQ checklist request published: correlationId={}, templateId={}",
                request.getCorrelationId(), request.getTemplateId());
    }
}
