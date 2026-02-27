package com.dojangkok.backend.repository;

import com.dojangkok.backend.domain.ChecklistTemplate;
import com.dojangkok.backend.domain.enums.ChecklistTemplateStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChecklistTemplateRepository extends JpaRepository<ChecklistTemplate, Long> {

    Optional<ChecklistTemplate> findByLifestyleVersionId(Long lifestyleVersionId);

    List<ChecklistTemplate> findAllByLifestyleVersionIdIn(List<Long> lifestyleVersionIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ChecklistTemplate ct WHERE ct.lifestyleVersion.id IN :versionIds")
    void deleteAllByLifestyleVersionIdIn(@Param("versionIds") List<Long> versionIds);

    @Query("SELECT ct FROM ChecklistTemplate ct " +
            "JOIN FETCH ct.lifestyleVersion lv " +
            "JOIN FETCH lv.lifestyle l " +
            "JOIN FETCH l.member " +
            "WHERE ct.checklistTemplateStatus = :status AND ct.createdAt < :threshold")
    List<ChecklistTemplate> findAllByStatusAndCreatedAtBefore(
            @Param("status") ChecklistTemplateStatus checklistTemplateStatus,
            @Param("threshold") LocalDateTime threshold);
}
