package com.dojangkok.backend.repository;

import com.dojangkok.backend.domain.Lifestyle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LifestyleRepository extends JpaRepository<Lifestyle, Long> {

    Optional<Lifestyle> findByMemberId(Long memberId);
    void deleteByMemberId(Long memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Lifestyle l SET l.currentVersion = null WHERE l.id = :lifestyleId")
    void clearCurrentVersion(@Param("lifestyleId") Long lifestyleId);
}
