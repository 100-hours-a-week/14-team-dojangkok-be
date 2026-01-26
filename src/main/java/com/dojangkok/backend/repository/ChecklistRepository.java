package com.dojangkok.backend.repository;

import com.dojangkok.backend.domain.Checklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChecklistRepository extends JpaRepository<Checklist, Long> {

    List<Checklist> findAllByMemberId(Long memberId);

    void deleteAllByMemberId(Long memberId);

    Optional<Checklist> findByHomeNoteId(Long homeNoteId);

    boolean existsByHomeNoteId(Long homeNoteId);
}
