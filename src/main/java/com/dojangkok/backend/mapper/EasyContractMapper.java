package com.dojangkok.backend.mapper;

import com.dojangkok.backend.domain.EasyContract;
import com.dojangkok.backend.domain.EasyContractFile;
import com.dojangkok.backend.dto.easycontract.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EasyContractMapper {

    public EasyContractCreateResponseDto toEasyContractCreateResponseDto(EasyContract easyContract) {
        return EasyContractCreateResponseDto.builder()
                .easyContractId(easyContract.getId())
                .status(easyContract.getStatus())
                .title(easyContract.getTitle())
                .content(easyContract.getContent())
                .createdAt(easyContract.getCreatedAt())
                .build();
    }

    public EasyContractListItemDto toEasyContractListItemDto(EasyContract easyContract) {
        return EasyContractListItemDto.builder()
                .easyContractId(easyContract.getId())
                .title(easyContract.getTitle())
                .status(easyContract.getStatus())
                .createdAt(easyContract.getCreatedAt())
                .updatedAt(easyContract.getUpdatedAt())
                .build();
    }

    public EasyContractListResponseDto toEasyContractListResponseDto(List<EasyContractListItemDto> items,
                                                                      int limit, boolean hasNext, String nextCursor) {
        return EasyContractListResponseDto.builder()
                .easyContractListItemList(items)
                .limit(limit)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }

    public EasyContractDetailResponseDto toEasyContractDetailResponseDto(EasyContract easyContract) {
        return EasyContractDetailResponseDto.builder()
                .easyContractId(easyContract.getId())
                .title(easyContract.getTitle())
                .content(easyContract.getContent())
                .status(easyContract.getStatus())
                .createdAt(easyContract.getCreatedAt())
                .updatedAt(easyContract.getUpdatedAt())
                .build();
    }

    public EasyContractRetryResponseDto toEasyContractRetryResponseDto(EasyContract easyContract) {
        return EasyContractRetryResponseDto.builder()
                .easyContractId(easyContract.getId())
                .status(easyContract.getStatus())
                .updatedAt(easyContract.getUpdatedAt())
                .build();
    }

    public EasyContractUpdateResponseDto toEasyContractUpdateResponseDto(EasyContract easyContract) {
        return EasyContractUpdateResponseDto.builder()
                .easyContractId(easyContract.getId())
                .title(easyContract.getTitle())
                .updatedAt(easyContract.getUpdatedAt())
                .build();
    }

    public EasyContractAssetItemDto toEasyContractAssetItemDto(EasyContractFile easyContractFile, String presignedUrl) {
        return EasyContractAssetItemDto.builder()
                .easyContractFileId(easyContractFile.getId())
                .fileAssetId(easyContractFile.getFileAsset().getId())
                .fileType(easyContractFile.getFileAsset().getFileType())
                .assetStatus(easyContractFile.getFileAsset().getStatus())
                .sortOrder(easyContractFile.getSortOrder())
                .presignedUrl(presignedUrl)
                .createdAt(easyContractFile.getCreatedAt())
                .build();
    }

    public EasyContractAssetListResponseDto toEasyContractAssetListResponseDto(List<EasyContractAssetItemDto> fileAssetList) {
        return EasyContractAssetListResponseDto.builder()
                .fileAssetList(fileAssetList)
                .build();
    }
}
