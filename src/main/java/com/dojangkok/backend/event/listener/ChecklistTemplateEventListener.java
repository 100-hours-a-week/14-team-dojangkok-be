package com.dojangkok.backend.event.listener;

import com.dojangkok.backend.event.ChecklistTemplateCreatedEvent;
import com.dojangkok.backend.service.OutboxEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChecklistTemplateEventListener {

    private final OutboxEventService outboxEventService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChecklistTemplateCreatedEvent(ChecklistTemplateCreatedEvent event) {
        log.info("Received ChecklistTemplateCreatedEvent: outboxEventId={}", event.outboxEventId());

        try {
            outboxEventService.publishAndMark(event.outboxEventId());
        } catch (Exception e) {
            log.error("Failed to handle ChecklistTemplateCreatedEvent: outboxEventId={}",
                    event.outboxEventId(), e);
        }
    }
}
