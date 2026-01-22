package com.dojangkok.backend.domain;

import com.dojangkok.backend.common.entity.BaseCreatedTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "easy_contract_file",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_easy_contract_file_contract_id_sort_order",
                        columnNames = {"easy_contract_id", "sort_order"}
                ),
                @UniqueConstraint(
                        name = "uk_easy_contract_file_asset_id",
                        columnNames = {"file_asset_id"}
                )
        }
)
public class EasyContractFile extends BaseCreatedTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "easy_contract_id", nullable = false)
    private EasyContract easyContract;

    // OneToOne 대신 ManyToOne으로 열어두고 unique로 제약
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_asset_id", nullable = false)
    private FileAsset fileAsset;

    @Builder(access = AccessLevel.PRIVATE)
    private EasyContractFile(EasyContract easyContract, FileAsset fileAsset, int sortOrder) {
        this.easyContract = easyContract;
        this.fileAsset = fileAsset;
        this.sortOrder = sortOrder;
    }

    public static EasyContractFile createEasyContractFile(EasyContract easyContract, FileAsset fileAsset, int sortOrder) {
        return EasyContractFile.builder()
                .easyContract(easyContract)
                .fileAsset(fileAsset)
                .sortOrder(sortOrder)
                .build();
    }
}

