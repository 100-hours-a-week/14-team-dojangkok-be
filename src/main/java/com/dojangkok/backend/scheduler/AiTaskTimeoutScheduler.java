package com.dojangkok.backend.scheduler;

import com.dojangkok.backend.domain.ChecklistTemplate;
import com.dojangkok.backend.domain.EasyContract;
import com.dojangkok.backend.domain.enums.ChecklistTemplateStatus;
import com.dojangkok.backend.domain.enums.EasyContractStatus;
import com.dojangkok.backend.repository.ChecklistTemplateRepository;
import com.dojangkok.backend.repository.EasyContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiTaskTimeoutScheduler {

    private static final int TIMEOUT_MINUTES = 10;
    private static final String REDIS_KEY_PREFIX = "sse:events:";

    private final EasyContractRepository easyContractRepository;
    private final ChecklistTemplateRepository checklistTemplateRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void checkTimeouts() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(TIMEOUT_MINUTES);

        checkEasyContractTimeouts(threshold);
        checkChecklistTimeouts(threshold);
    }

    private void checkEasyContractTimeouts(LocalDateTime threshold) {
        List<EasyContract> timedOut = easyContractRepository
                .findAllByStatusAndCreatedAtBefore(EasyContractStatus.PROCESSING, threshold);

        for (EasyContract contract : timedOut) {
            contract.markFailed();
            easyContractRepository.save(contract);

            pushTimeoutEvent(contract.getMember().getId(), Map.of(
                    "type", "easy-contract",
                    "easy_contract_id", contract.getId(),
                    "success", false,
                    "error_message", "작업 시간이 초과되었습니다."
            ));

            log.warn("EasyContract timed out: id={}, memberId={}",
                    contract.getId(), contract.getMember().getId());
        }

        if (!timedOut.isEmpty()) {
            log.info("EasyContract timeout processed: count={}", timedOut.size());
        }
    }

    private void checkChecklistTimeouts(LocalDateTime threshold) {
        List<ChecklistTemplate> timedOut = checklistTemplateRepository
                .findAllByStatusAndCreatedAtBefore(ChecklistTemplateStatus.PROCESSING, threshold);

        for (ChecklistTemplate template : timedOut) {
            template.updateStatus(ChecklistTemplateStatus.FAILED);
            checklistTemplateRepository.save(template);

            Long memberId = template.getLifestyleVersion().getLifestyle().getMember().getId();

            pushTimeoutEvent(memberId, Map.of(
                    "type", "checklist",
                    "template_id", template.getId(),
                    "success", false,
                    "error_message", "작업 시간이 초과되었습니다."
            ));

            log.warn("Checklist timed out: templateId={}, memberId={}",
                    template.getId(), memberId);
        }

        if (!timedOut.isEmpty()) {
            log.info("Checklist timeout processed: count={}", timedOut.size());
        }
    }

    private void pushTimeoutEvent(Long memberId, Map<String, Object> event) {
        if (memberId == null) return;

        try {
            String key = REDIS_KEY_PREFIX + memberId;
            String value = objectMapper.writeValueAsString(event);
            redisTemplate.opsForList().rightPush(key, value);
            redisTemplate.expire(key, Duration.ofMinutes(5));
        } catch (Exception e) {
            log.error("Failed to push timeout event to Redis: memberId={}", memberId, e);
        }
    }
}
