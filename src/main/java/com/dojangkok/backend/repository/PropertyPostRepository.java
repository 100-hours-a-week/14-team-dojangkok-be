package com.dojangkok.backend.repository;

import com.dojangkok.backend.domain.PropertyPost;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyPostRepository extends JpaRepository<PropertyPost, Long>, PropertyPostSearchRepository {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE PropertyPost pp SET pp.member.id = :newMemberId WHERE pp.member.id = :memberId")
    void updateMemberIdByMemberId(@Param("memberId") Long memberId, @Param("newMemberId") Long newMemberId);

    @Query("SELECT pp FROM PropertyPost pp WHERE pp.id = :id AND pp.deletedAt IS NULL")
    Optional<PropertyPost> findByIdAndNotDeleted(@Param("id") Long id);

    @Query("SELECT pp FROM PropertyPost pp " +
            "WHERE pp.deletedAt IS NULL " +
            "AND pp.isHidden = false " +
            "AND pp.postStatus = com.dojangkok.backend.domain.enums.PostStatus.ACTIVE " +
            "AND pp.dealStatus = com.dojangkok.backend.domain.enums.DealStatus.TRADING " +
            "ORDER BY pp.createdAt DESC, pp.id DESC")
    List<PropertyPost> findAllActiveAndVisible(Pageable pageable);

    @Query("SELECT COUNT(pp) FROM PropertyPost pp " +
            "WHERE pp.deletedAt IS NULL " +
            "AND pp.isHidden = false " +
            "AND pp.postStatus = com.dojangkok.backend.domain.enums.PostStatus.ACTIVE " +
            "AND pp.dealStatus = com.dojangkok.backend.domain.enums.DealStatus.TRADING")
    long countAllActiveAndVisible();

    @Query("SELECT pp FROM PropertyPost pp " +
            "WHERE pp.deletedAt IS NULL " +
            "AND pp.isHidden = false " +
            "AND pp.postStatus = com.dojangkok.backend.domain.enums.PostStatus.ACTIVE " +
            "AND pp.dealStatus = com.dojangkok.backend.domain.enums.DealStatus.TRADING " +
            "AND pp.id < :cursorId " +
            "ORDER BY pp.createdAt DESC, pp.id DESC")
    List<PropertyPost> findAllActiveAndVisibleWithCursor(@Param("cursorId") Long cursorId, Pageable pageable);

    @Query("SELECT pp FROM PropertyPost pp WHERE pp.id = :id")
    Optional<PropertyPost> findByIdIncludingDeleted(@Param("id") Long id);

    // 숨긴 매물 목록 (작성자 본인, is_hidden=true, 삭제되지 않은 것)
    @Query("SELECT pp FROM PropertyPost pp " +
            "WHERE pp.member.id = :memberId " +
            "AND pp.deletedAt IS NULL " +
            "AND pp.isHidden = true " +
            "ORDER BY pp.createdAt DESC, pp.id DESC")
    List<PropertyPost> findAllHiddenByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    @Query("SELECT COUNT(pp) FROM PropertyPost pp " +
            "WHERE pp.member.id = :memberId " +
            "AND pp.deletedAt IS NULL " +
            "AND pp.isHidden = true")
    long countAllHiddenByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT pp FROM PropertyPost pp " +
            "WHERE pp.member.id = :memberId " +
            "AND pp.deletedAt IS NULL " +
            "AND pp.isHidden = true " +
            "AND pp.id < :cursorId " +
            "ORDER BY pp.createdAt DESC, pp.id DESC")
    List<PropertyPost> findAllHiddenByMemberIdWithCursor(@Param("memberId") Long memberId, @Param("cursorId") Long cursorId, Pageable pageable);

    // 거래 완료 목록 (작성자 본인, is_hidden=false, deal_status=COMPLETED, 삭제되지 않은 것)
    @Query("SELECT pp FROM PropertyPost pp " +
            "WHERE pp.member.id = :memberId " +
            "AND pp.deletedAt IS NULL " +
            "AND pp.isHidden = false " +
            "AND pp.dealStatus = com.dojangkok.backend.domain.enums.DealStatus.COMPLETED " +
            "ORDER BY pp.createdAt DESC, pp.id DESC")
    List<PropertyPost> findAllCompletedByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    @Query("SELECT COUNT(pp) FROM PropertyPost pp " +
            "WHERE pp.member.id = :memberId " +
            "AND pp.deletedAt IS NULL " +
            "AND pp.isHidden = false " +
            "AND pp.dealStatus = com.dojangkok.backend.domain.enums.DealStatus.COMPLETED")
    long countAllCompletedByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT pp FROM PropertyPost pp " +
            "WHERE pp.member.id = :memberId " +
            "AND pp.deletedAt IS NULL " +
            "AND pp.isHidden = false " +
            "AND pp.dealStatus = com.dojangkok.backend.domain.enums.DealStatus.COMPLETED " +
            "AND pp.id < :cursorId " +
            "ORDER BY pp.createdAt DESC, pp.id DESC")
    List<PropertyPost> findAllCompletedByMemberIdWithCursor(@Param("memberId") Long memberId, @Param("cursorId") Long cursorId, Pageable pageable);

    // 거래 중 목록 (작성자 본인, is_hidden=false, deal_status=TRADING, 삭제되지 않은 것)
    @Query("SELECT pp FROM PropertyPost pp " +
            "WHERE pp.member.id = :memberId " +
            "AND pp.deletedAt IS NULL " +
            "AND pp.isHidden = false " +
            "AND pp.dealStatus = com.dojangkok.backend.domain.enums.DealStatus.TRADING " +
            "ORDER BY pp.createdAt DESC, pp.id DESC")
    List<PropertyPost> findAllTradingByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    @Query("SELECT COUNT(pp) FROM PropertyPost pp " +
            "WHERE pp.member.id = :memberId " +
            "AND pp.deletedAt IS NULL " +
            "AND pp.isHidden = false " +
            "AND pp.dealStatus = com.dojangkok.backend.domain.enums.DealStatus.TRADING")
    long countAllTradingByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT pp FROM PropertyPost pp " +
            "WHERE pp.member.id = :memberId " +
            "AND pp.deletedAt IS NULL " +
            "AND pp.isHidden = false " +
            "AND pp.dealStatus = com.dojangkok.backend.domain.enums.DealStatus.TRADING " +
            "AND pp.id < :cursorId " +
            "ORDER BY pp.createdAt DESC, pp.id DESC")
    List<PropertyPost> findAllTradingByMemberIdWithCursor(@Param("memberId") Long memberId, @Param("cursorId") Long cursorId, Pageable pageable);
}
