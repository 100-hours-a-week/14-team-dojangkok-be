package com.dojangkok.backend.sse;

import com.dojangkok.backend.mq.dto.AiResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class SsePollingScheduler {

    private final StringRedisTemplate redisTemplate;
    private final SseEmitterStore emitterStore;
    private final ObjectMapper objectMapper;

    private static final String REDIS_KEY_PREFIX = "sse:events:";

    @Scheduled(fixedDelay = 500)
    public void poll() {
        if (emitterStore.isEmpty()) return;

        Set<Long> memberIds = emitterStore.getAllMemberIds();
        List<Long> idList = new ArrayList<>(memberIds);

        // Pipeline: Redis 왕복 1회로 모든 memberId의 첫 번째 이벤트 조회
        List<Object> results = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (Long memberId : idList) {
                byte[] key = (REDIS_KEY_PREFIX + memberId).getBytes(StandardCharsets.UTF_8);
                connection.listCommands().lPop(key);
            }
            return null;
        });

        for (int i = 0; i < idList.size(); i++) {
            String value = (String) results.get(i);
            if (value == null) continue;

            Long memberId = idList.get(i);
            SseEmitter emitter = emitterStore.get(memberId);
            if (emitter == null) continue;

            // 첫 번째 이벤트 push
            if (!sendEvent(memberId, emitter, value)) continue;

            // 추가 이벤트가 있을 수 있으니 나머지도 꺼냄
            drainRemaining(memberId, emitter);
        }
    }

    private void drainRemaining(Long memberId, SseEmitter emitter) {
        String key = REDIS_KEY_PREFIX + memberId;
        String value;
        while ((value = redisTemplate.opsForList().leftPop(key)) != null) {
            if (!sendEvent(memberId, emitter, value)) break;
        }
    }

    private boolean sendEvent(Long memberId, SseEmitter emitter, String value) {
        try {
            AiResponseDto response = objectMapper.readValue(value, AiResponseDto.class);

            // type 필드로 SSE 이벤트 이름 결정
            String eventName = resolveEventName(response.getType());

            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(value));  // 원본 JSON 그대로 전달

            log.info("SSE push: memberId={}, type={}, correlationId={}",
                    memberId, response.getType(), response.getCorrelationId());
            return true;
        } catch (IOException e) {
            emitterStore.remove(memberId);
            log.error("SSE push failed: memberId={}", memberId, e);
            return false;
        }
    }

    private String resolveEventName(String type) {
        if (type == null) return "unknown";
        return switch (type) {
            case "easy-contract" -> "easy-contract-result";
            case "checklist" -> "checklist-result";
            default -> type;
        };
    }
}
