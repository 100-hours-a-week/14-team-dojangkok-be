package com.dojangkok.backend.repository;

import com.dojangkok.backend.domain.HomeNote;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HomeNoteRepository extends JpaRepository<HomeNote, Long> {

    List<HomeNote> findAllByMemberId(Long memberId);

    void deleteAllByMemberId(Long memberId);

    @Query("SELECT hn FROM HomeNote hn " +
            "WHERE hn.member.id = :memberId " +
            "AND hn.deletedAt IS NULL " +
            "ORDER BY hn.createdAt DESC")
    List<HomeNote> findAllByMemberIdAndNotDeleted(@Param("memberId") Long memberId, Pageable pageable);

    @Query("SELECT hn FROM HomeNote hn " +
            "WHERE hn.member.id = :memberId " +
            "AND hn.deletedAt IS NULL " +
            "AND hn.id < :cursorId " +
            "ORDER BY hn.createdAt DESC")
    List<HomeNote> findAllByMemberIdAndNotDeletedWithCursor(
            @Param("memberId") Long memberId,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("SELECT hn FROM HomeNote hn " +
            "WHERE hn.id = :homeNoteId " +
            "AND hn.deletedAt IS NULL")
    Optional<HomeNote> findByIdAndNotDeleted(@Param("homeNoteId") Long homeNoteId);
}
