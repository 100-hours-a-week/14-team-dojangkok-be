package com.dojangkok.backend.mq;

import com.dojangkok.backend.mq.config.RabbitMQConfig;
import com.dojangkok.backend.mq.dto.EasyContractCancelRequestDto;
import com.dojangkok.backend.mq.dto.EasyContractMqRequestDto;
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
                RabbitMQConfig.WAS_EXCHANGE, "quorum.easy-contract.request", request);

        log.info("MQ request published: correlationId={}, easyContractId={}",
                request.getCorrelationId(), request.getEasyContractId());
    }

    public void sendCancel(EasyContractCancelRequestDto request) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.WAS_EXCHANGE, "quorum.cancel.request", request);

        log.info("MQ cancel request published: easyContractId={}",
                request.getEasyContractId());
    }
}
