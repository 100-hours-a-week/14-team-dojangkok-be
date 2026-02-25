package com.dojangkok.backend.mq;

import com.dojangkok.backend.mq.dto.EasyContractMqRequestDto;
import com.dojangkok.backend.mq.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EasyContractMqProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendRequest(EasyContractMqRequestDto request) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.WAS_EXCHANGE, "easy-contract.request", request);

        log.info("MQ request published: correlationId={}, easyContractId={}",
                request.getCorrelationId(), request.getEasyContractId());
    }
}
