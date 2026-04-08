package com.dojangkok.backend.repository;

import com.dojangkok.backend.domain.OutboxEvent;
import com.dojangkok.backend.domain.enums.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findAllByStatusAndCreatedAtBefore(OutboxStatus status, LocalDateTime threshold);

    void deleteAllByStatusAndPublishedAtBefore(OutboxStatus status, LocalDateTime threshold);
}
