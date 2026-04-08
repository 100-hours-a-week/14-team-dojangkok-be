package com.dojangkok.backend.mq;

import com.dojangkok.backend.mq.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Slf4j
@Component
@RequiredArgsConstructor
public class DlqReprocessConsumer {

    private final RabbitTemplate rabbitTemplate;
    private final DiscordAlertService discordAlertService;

    private static final int MAX_RETRY = 3;
    private static final long INITIAL_DELAY_MS = 5000; // 5초
    private static final long MAX_DELAY_MS = 60000;    // 최대 60초


    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_DLQ)
    public void handleNotificationDlq(Message message) {
        reprocess(message,
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                "quorum.notification",
                "notification");
    }

    @RabbitListener(queues = RabbitMQConfig.DATA_EVENTS_DLQ)
    public void handleDataEventsDlq(Message message) {
        reprocess(message,
                RabbitMQConfig.DATA_EVENTS_EXCHANGE,
                "quorum.data",
                "data-events");
    }

    @RabbitListener(queues = RabbitMQConfig.CHECKLIST_REQUEST_DLQ)
    public void handleChecklistDlq(Message message) {
        reprocess(message,
                RabbitMQConfig.WAS_EXCHANGE,
                "quorum.checklist.request",
                "checklist");
    }

    //  쉬운 계약서 DLQ — 재처리 없이 디스코드 알림만
    @RabbitListener(queues = RabbitMQConfig.EASY_CONTRACT_REQUEST_DLQ)
    public void handleEasyContractDlq(Message message) {
        String messageId = message.getMessageProperties().getMessageId();
        String body = new String(message.getBody());
        long retryCount = getRetryCount(message);

        log.error("[DLQ-easy-contract] 쉬운 계약서 요청 실패 → 디스코드 알림. messageId={}, retryCount={}",
                messageId, retryCount);

        discordAlertService.sendDlqAlert(
                RabbitMQConfig.EASY_CONTRACT_REQUEST_DLQ,
                messageId,
                body,
                "Spring Retry " + retryCount + "회 실패 후 DLQ 적재"
        );
        // ack 처리되어 DLQ에서 제거
    }

    private void reprocess(Message message, String exchange, String routingKey, String label) {
        long retryCount = getRetryCount(message);

        if (retryCount >= MAX_RETRY) {
            log.error("[DLQ-{}] 최대 재시도 횟수 초과 ({}회). 메시지 폐기. messageId={}, body={}",
                    label, retryCount,
                    message.getMessageProperties().getMessageId(),
                    new String(message.getBody()));
            return; // ack 처리되어 DLQ에서 제거됨
        }

        // 지수 백오프 지연: 5초 → 10초 → 20초
        long delay = Math.min(INITIAL_DELAY_MS * (long) Math.pow(2, retryCount), MAX_DELAY_MS);

        log.info("[DLQ-{}] 재처리 시도 ({}회차, {}ms 후 재발행). messageId={}",
                label, retryCount + 1, delay,
                message.getMessageProperties().getMessageId());

        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[DLQ-{}] 재처리 지연 중 인터럽트 발생", label);
            return;
        }

        try {
            rabbitTemplate.send(exchange, routingKey, message);
            log.info("[DLQ-{}] 원본 큐로 재발행 완료 ({}회차). exchange={}, routingKey={}",
                    label, retryCount + 1, exchange, routingKey);
        } catch (Exception e) {
            log.error("[DLQ-{}] 재발행 실패 ({}회차). exchange={}, routingKey={}",
                    label, retryCount + 1, exchange, routingKey, e);
        }
    }

    private long getRetryCount(Message message) {
        try {
            List<Map<String, Object>> xDeathHeaders =
                    message.getMessageProperties().getHeader("x-death");

            if (xDeathHeaders == null || xDeathHeaders.isEmpty()) {
                return 0;
            }

            // x-death의 첫 번째 항목에서 count를 읽음
            Object count = xDeathHeaders.getFirst().get("count");
            if (count instanceof Number) {
                return ((Number) count).longValue();
            }
            return 0;
        } catch (Exception e) {
            log.warn("x-death header 파싱 실패, retryCount=0으로 처리", e);
            return 0;
        }
    }
}
