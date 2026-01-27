package com.dojangkok.backend.repository;

import com.dojangkok.backend.domain.PropertyPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyPostRepository extends JpaRepository<PropertyPost, Long> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE PropertyPost pp SET pp.member.id = :newMemberId WHERE pp.member.id = :memberId")
    void updateMemberIdByMemberId(@Param("memberId") Long memberId, @Param("newMemberId") Long newMemberId);
}
