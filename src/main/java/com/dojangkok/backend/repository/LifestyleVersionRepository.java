package com.dojangkok.backend.repository;

import com.dojangkok.backend.domain.LifestyleVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LifestyleVersionRepository extends JpaRepository<LifestyleVersion, Long> {

    Optional<LifestyleVersion> findTopByLifestyleIdOrderByVersionNoDesc(Long lifestyleId);

    List<LifestyleVersion> findAllByLifestyleId(Long lifestyleId);

    @Modifying
    @Query("DELETE FROM LifestyleVersion lv WHERE lv.lifestyle.id = :lifestyleId")
    void deleteAllByLifestyleId(@Param("lifestyleId") Long lifestyleId);
}
