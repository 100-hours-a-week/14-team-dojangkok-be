package com.dojangkok.backend.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
public class DiscordAlertService {

    private final RestClient restClient;
    private final String webhookUrl;

    public DiscordAlertService(
            @Value("${discord.webhook.url:}") String webhookUrl) {
        this.restClient = RestClient.create();
        this.webhookUrl = webhookUrl;
    }

    public void sendDlqAlert(String dlqName, String messageId, String body, String errorInfo) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("[Discord] Webhook URL이 설정되지 않아 알림을 전송하지 않습니다. discord.webhook.url을 확인하세요.");
            return;
        }

        try {
            Map<String, Object> payload = buildPayload(dlqName, messageId, body, errorInfo);

            restClient.post()
                    .uri(webhookUrl)
                    .header("Content-Type", "application/json")
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            log.info("[Discord] DLQ 알림 전송 완료: dlq={}, messageId={}", dlqName, messageId);
        } catch (Exception e) {
            log.error("[Discord] DLQ 알림 전송 실패: dlq={}, messageId={}", dlqName, messageId, e);
        }
    }

    private Map<String, Object> buildPayload(String dlqName, String messageId, String body, String errorInfo) {
        String truncatedBody = body != null && body.length() > 500
                ? body.substring(0, 500) + "..."
                : body;

        Map<String, Object> embed = Map.of(
                "title", "🚨 DLQ 메시지 적재 알림",
                "color", 15548997,
                "fields", List.of(
                        Map.of("name", "DLQ", "value", dlqName, "inline", true),
                        Map.of("name", "Message ID", "value", messageId != null ? messageId : "N/A", "inline", true),
                        Map.of("name", "발생 시각", "value", Instant.now().toString(), "inline", false),
                        Map.of("name", "에러 정보", "value", errorInfo != null ? errorInfo : "N/A", "inline", false),
                        Map.of("name", "메시지 본문", "value", "```json\n" + truncatedBody + "\n```", "inline", false)
                ),
                "footer", Map.of("text", "DojangKok DLQ Monitor")
        );

        return Map.of("embeds", List.of(embed));
    }
}
