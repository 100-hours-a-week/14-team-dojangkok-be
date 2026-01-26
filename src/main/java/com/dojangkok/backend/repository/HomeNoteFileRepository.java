package com.dojangkok.backend.repository;

import com.dojangkok.backend.domain.HomeNoteFile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HomeNoteFileRepository extends JpaRepository<HomeNoteFile, Long> {

    List<HomeNoteFile> findAllByHomeNoteId(Long homeNoteId);

    @Modifying
    @Query("DELETE FROM HomeNoteFile hnf WHERE hnf.homeNote.id IN :homeNoteIds")
    void deleteAllByHomeNoteIdIn(@Param("homeNoteIds") List<Long> homeNoteIds);

    @Query("SELECT COUNT(hnf) FROM HomeNoteFile hnf WHERE hnf.homeNote.id = :homeNoteId")
    int countByHomeNoteId(@Param("homeNoteId") Long homeNoteId);

    @Query("SELECT hnf FROM HomeNoteFile hnf " +
            "JOIN FETCH hnf.fileAsset " +
            "WHERE hnf.homeNote.id = :homeNoteId " +
            "ORDER BY hnf.sortOrder ASC")
    List<HomeNoteFile> findAllByHomeNoteIdWithFileAsset(@Param("homeNoteId") Long homeNoteId, Pageable pageable);

    @Query("SELECT hnf FROM HomeNoteFile hnf " +
            "JOIN FETCH hnf.fileAsset " +
            "WHERE hnf.homeNote.id = :homeNoteId " +
            "AND hnf.id > :cursorId " +
            "ORDER BY hnf.sortOrder ASC")
    List<HomeNoteFile> findAllByHomeNoteIdWithFileAssetAndCursor(
            @Param("homeNoteId") Long homeNoteId,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("SELECT hnf FROM HomeNoteFile hnf " +
            "JOIN FETCH hnf.fileAsset " +
            "WHERE hnf.homeNote.id = :homeNoteId " +
            "ORDER BY hnf.sortOrder ASC")
    List<HomeNoteFile> findTop10ByHomeNoteIdWithFileAsset(@Param("homeNoteId") Long homeNoteId, Pageable pageable);

    @Query("SELECT MAX(hnf.sortOrder) FROM HomeNoteFile hnf WHERE hnf.homeNote.id = :homeNoteId")
    Optional<Integer> findMaxSortOrderByHomeNoteId(@Param("homeNoteId") Long homeNoteId);

    Optional<HomeNoteFile> findByIdAndHomeNoteId(Long id, Long homeNoteId);

    boolean existsByFileAssetId(Long fileAssetId);
}
