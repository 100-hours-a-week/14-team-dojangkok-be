package com.dojangkok.backend.repository;

import com.dojangkok.backend.domain.FileAsset;
import com.dojangkok.backend.domain.enums.FileAssetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FileAssetRepository extends JpaRepository<FileAsset, Long> {

    List<FileAsset> findAllByIdIn(List<Long> ids);

}
