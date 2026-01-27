package com.dojangkok.backend.repository;

import com.dojangkok.backend.domain.ChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Long> {

    @Modifying
    @Query("DELETE FROM ChecklistItem ci WHERE ci.checklist.id IN :checklistIds")
    void deleteAllByChecklistIdIn(@Param("checklistIds") List<Long> checklistIds);

    List<ChecklistItem> findAllByChecklistId(Long checklistId);

    @Modifying
    @Query("DELETE FROM ChecklistItem ci WHERE ci.checklist.id = :checklistId")
    void deleteAllByChecklistId(@Param("checklistId") Long checklistId);

    Optional<ChecklistItem> findByIdAndChecklistId(Long itemId, Long checklistId);
}
