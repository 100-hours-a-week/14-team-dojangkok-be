package com.dojangkok.backend.event.listener;

import com.dojangkok.backend.event.LifestyleCreatedEvent;
import com.dojangkok.backend.service.ChecklistTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class LifestyleEventListener {

    private final ChecklistTemplateService checklistTemplateService;

    /**
     * 라이프스타일 생성 이벤트를 수신하여 체크리스트 템플릿 생성을 시작
     * 트랜잭션 커밋 후 비동기로 실행
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLifestyleCreatedEvent(LifestyleCreatedEvent event) {
        log.info("Received LifestyleCreatedEvent for lifestyleVersionId: {}", event.lifestyleVersionId());

        try {
            checklistTemplateService.requestChecklistGeneration(
                    event.memberId(),
                    event.lifestyleVersionId(),
                    event.lifestyleItems()
            );
        } catch (Exception e) {
            log.error("Failed to handle LifestyleCreatedEvent for lifestyleVersionId: {}",
                    event.lifestyleVersionId(), e);
        }
    }
}
