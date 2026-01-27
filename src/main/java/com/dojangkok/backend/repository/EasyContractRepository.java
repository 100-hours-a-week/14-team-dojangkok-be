package com.dojangkok.backend.repository;

import com.dojangkok.backend.domain.EasyContract;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EasyContractRepository extends JpaRepository<EasyContract, Long> {

    List<EasyContract> findAllByMemberId(Long memberId);

    void deleteAllByMemberId(Long memberId);

    @Query("SELECT ec FROM EasyContract ec WHERE ec.member.id = :memberId AND ec.deletedAt IS NULL ORDER BY ec.createdAt DESC, ec.id DESC")
    List<EasyContract> findAllByMemberIdAndNotDeleted(@Param("memberId") Long memberId, Pageable pageable);

    @Query("SELECT ec FROM EasyContract ec WHERE ec.member.id = :memberId AND ec.deletedAt IS NULL AND ec.id < :cursorId ORDER BY ec.createdAt DESC, ec.id DESC")
    List<EasyContract> findAllByMemberIdAndNotDeletedWithCursor(@Param("memberId") Long memberId, @Param("cursorId") Long cursorId, Pageable pageable);

    @Query("SELECT ec FROM EasyContract ec WHERE ec.id = :id AND ec.deletedAt IS NULL")
    Optional<EasyContract> findByIdAndNotDeleted(@Param("id") Long id);

    @Query("SELECT COUNT(ec) FROM EasyContract ec WHERE ec.member.id = :memberId AND ec.status = 'COMPLETED' AND ec.deletedAt IS NULL")
    int countCompletedByMemberId(@Param("memberId") Long memberId);
}
