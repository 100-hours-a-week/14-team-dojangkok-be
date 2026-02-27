package com.dojangkok.backend.repository;

import com.dojangkok.backend.domain.PropertyPostFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyPostFileRepository extends JpaRepository<PropertyPostFile, Long> {

    @Query("SELECT ppf FROM PropertyPostFile ppf " +
            "JOIN FETCH ppf.fileAsset " +
            "WHERE ppf.propertyPost.id = :propertyPostId " +
            "ORDER BY ppf.sortOrder ASC")
    List<PropertyPostFile> findAllByPropertyPostIdWithFileAsset(@Param("propertyPostId") Long propertyPostId);

    @Query("SELECT MAX(ppf.sortOrder) FROM PropertyPostFile ppf WHERE ppf.propertyPost.id = :propertyPostId")
    Optional<Integer> findMaxSortOrderByPropertyPostId(@Param("propertyPostId") Long propertyPostId);

    @Query("SELECT ppf FROM PropertyPostFile ppf " +
            "JOIN FETCH ppf.fileAsset " +
            "WHERE ppf.fileAsset.id = :fileAssetId AND ppf.propertyPost.id = :propertyPostId")
    Optional<PropertyPostFile> findByFileAssetIdAndPropertyPostId(
            @Param("fileAssetId") Long fileAssetId,
            @Param("propertyPostId") Long propertyPostId);

    @Query("SELECT ppf FROM PropertyPostFile ppf " +
            "WHERE ppf.propertyPost.id = :propertyPostId " +
            "AND ppf.id <> :excludeId " +
            "ORDER BY ppf.sortOrder ASC " +
            "LIMIT 1")
    Optional<PropertyPostFile> findNextPrimaryCandidate(
            @Param("propertyPostId") Long propertyPostId,
            @Param("excludeId") Long excludeId);

    long countByPropertyPostId(@Param("propertyPostId") Long propertyPostId);

    @Query("SELECT ppf FROM PropertyPostFile ppf " +
            "JOIN FETCH ppf.fileAsset " +
            "JOIN FETCH ppf.propertyPost p " +
            "JOIN FETCH p.member " +
            "WHERE ppf.fileAsset.id = :fileAssetId")
    Optional<PropertyPostFile> findByFileAssetId(@Param("fileAssetId") Long fileAssetId);
}
