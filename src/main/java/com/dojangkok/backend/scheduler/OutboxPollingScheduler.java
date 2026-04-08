package com.dojangkok.backend.scheduler;

import com.dojangkok.backend.domain.OutboxEvent;
import com.dojangkok.backend.domain.enums.OutboxStatus;
import com.dojangkok.backend.repository.OutboxEventRepository;
import com.dojangkok.backend.service.OutboxEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPollingScheduler {

    private static final int PENDING_THRESHOLD_SECONDS = 30;
    private static final int CLEANUP_DAYS = 7;

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxEventService outboxEventService;

    @Scheduled(fixedDelay = 30000)
    public void pollPendingEvents() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(PENDING_THRESHOLD_SECONDS);

        List<OutboxEvent> pendingEvents = outboxEventRepository
                .findAllByStatusAndCreatedAtBefore(OutboxStatus.PENDING, threshold);

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Outbox polling found {} pending events", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            outboxEventService.retryPublish(event);
        }
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupPublishedEvents() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(CLEANUP_DAYS);
        outboxEventRepository.deleteAllByStatusAndPublishedAtBefore(OutboxStatus.PUBLISHED, threshold);
        log.info("Outbox cleanup completed: removed PUBLISHED events older than {} days", CLEANUP_DAYS);
    }
}
