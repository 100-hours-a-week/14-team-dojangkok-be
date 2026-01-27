package com.dojangkok.backend.repository;

import com.dojangkok.backend.domain.ChecklistTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
            "JOIN ct.lifestyleVersion lv " +
            "JOIN lv.lifestyle l " +
            "WHERE l.member.id = :memberId " +
            "ORDER BY ct.createdAt DESC " +
            "LIMIT 1")
    Optional<ChecklistTemplate> findTopByLifestyleVersionMemberIdOrderByCreatedAtDesc(@Param("memberId") Long memberId);
}
