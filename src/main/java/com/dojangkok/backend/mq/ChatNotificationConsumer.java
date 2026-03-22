package com.dojangkok.backend.mq;

import com.dojangkok.backend.mq.config.RabbitMQConfig;
import com.dojangkok.backend.mq.dto.ChatNotificationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatNotificationConsumer {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String REDIS_KEY_PREFIX = "sse:events:";

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleChatNotification(ChatNotificationDto notification,
                                        Channel channel,
                                        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.info("채팅 알림 수신: targetMemberId={}, roomId={}, messageId={}",
                notification.getTargetMemberId(), notification.getRoomId(), notification.getMessageId());

        try {
            pushToRedis(notification);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("채팅 알림 처리 실패: messageId={}", notification.getMessageId(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    private void pushToRedis(ChatNotificationDto notification) {
        Long memberId = notification.getTargetMemberId();
        if (memberId == null) return;

        try {
            String key = REDIS_KEY_PREFIX + memberId;
            String value = objectMapper.writeValueAsString(notification);
            redisTemplate.opsForList().rightPush(key, value);
            redisTemplate.expire(key, Duration.ofMinutes(5));
            log.info("채팅 알림 Redis 저장: memberId={}, roomId={}", memberId, notification.getRoomId());
        } catch (Exception e) {
            log.error("채팅 알림 Redis 저장 실패: memberId={}", memberId, e);
        }
    }
}
