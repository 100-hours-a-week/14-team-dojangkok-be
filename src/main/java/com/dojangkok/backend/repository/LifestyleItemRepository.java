package com.dojangkok.backend.repository;

import com.dojangkok.backend.domain.LifestyleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LifestyleItemRepository extends JpaRepository<LifestyleItem, Long> {

    List<LifestyleItem> findAllByLifestyleVersionId(Long lifestyleVersionId);

    @Modifying
    @Query("DELETE FROM LifestyleItem li WHERE li.lifestyleVersion.id IN :versionIds")
    void deleteAllByLifestyleVersionIdIn(@Param("versionIds") List<Long> versionIds);
}
