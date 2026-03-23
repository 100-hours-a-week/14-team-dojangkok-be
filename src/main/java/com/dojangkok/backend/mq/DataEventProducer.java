package com.dojangkok.backend.mq;

import com.dojangkok.backend.mq.config.RabbitMQConfig;
import com.dojangkok.backend.mq.dto.DataEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publishUserUpdated(Long memberId, String nickname, String profileImageUrl) {
        DataEventDto event = DataEventDto.builder()
                .type("USER_UPDATED")
                .userId(String.valueOf(memberId))
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .build();
        publish(event);
        log.info("데이터 이벤트 발행: type=USER_UPDATED, userId={}", memberId);
    }

    public void publishUserDeleted(Long memberId) {
        DataEventDto event = DataEventDto.builder()
                .type("USER_DELETED")
                .userId(String.valueOf(memberId))
                .build();
        publish(event);
        log.info("데이터 이벤트 발행: type=USER_DELETED, userId={}", memberId);
    }

    public void publishPropertyUpdated(Long propertyId, String title, String imageUrl, String dealStatus) {
        DataEventDto event = DataEventDto.builder()
                .type("PROPERTY_UPDATED")
                .propertyId(String.valueOf(propertyId))
                .title(title)
                .imageUrl(imageUrl)
                .dealStatus(dealStatus)
                .build();
        publish(event);
        log.info("데이터 이벤트 발행: type=PROPERTY_UPDATED, propertyId={}", propertyId);
    }

    public void publishPropertyDeleted(Long propertyId) {
        DataEventDto event = DataEventDto.builder()
                .type("PROPERTY_DELETED")
                .propertyId(String.valueOf(propertyId))
                .build();
        publish(event);
        log.info("데이터 이벤트 발행: type=PROPERTY_DELETED, propertyId={}", propertyId);
    }

    private void publish(DataEventDto event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DATA_EVENTS_EXCHANGE, "quorum.data", event);
    }
}
