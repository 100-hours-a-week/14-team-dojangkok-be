package com.dojangkok.backend.repository;

import com.dojangkok.backend.domain.ChecklistTemplateItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChecklistTemplateItemRepository extends JpaRepository<ChecklistTemplateItem, Long> {

    List<ChecklistTemplateItem> findAllByChecklistTemplateId(Long checklistTemplateId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ChecklistTemplateItem cti WHERE cti.checklistTemplate.id = :templateId")
    void deleteAllByChecklistTemplateId(@Param("templateId") Long templateId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ChecklistTemplateItem cti WHERE cti.checklistTemplate.id IN :templateIds")
    void deleteAllByChecklistTemplateIdIn(@Param("templateIds") List<Long> templateIds);
}
