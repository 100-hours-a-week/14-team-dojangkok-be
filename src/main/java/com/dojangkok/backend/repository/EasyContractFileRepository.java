package com.dojangkok.backend.repository;

import com.dojangkok.backend.domain.EasyContractFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EasyContractFileRepository extends JpaRepository<EasyContractFile, Long> {

    @Modifying
    @Query("DELETE FROM EasyContractFile ecf WHERE ecf.easyContract.id IN :easyContractIds")
    void deleteAllByEasyContractIdIn(@Param("easyContractIds") List<Long> easyContractIds);

    @Modifying
    @Query("DELETE FROM EasyContractFile ecf WHERE ecf.easyContract.id = :easyContractId")
    void deleteAllByEasyContractId(@Param("easyContractId") Long easyContractId);

    @Query("SELECT ecf FROM EasyContractFile ecf JOIN FETCH ecf.fileAsset WHERE ecf.easyContract.id = :easyContractId ORDER BY ecf.sortOrder ASC")
    List<EasyContractFile> findAllByEasyContractIdWithFileAsset(@Param("easyContractId") Long easyContractId);

    @Query("SELECT MAX(ecf.sortOrder) FROM EasyContractFile ecf WHERE ecf.easyContract.id = :easyContractId")
    Optional<Integer> findMaxSortOrderByEasyContractId(@Param("easyContractId") Long easyContractId);

    Optional<EasyContractFile> findByFileAssetIdAndEasyContractId(Long id, Long easyContractId);
}
