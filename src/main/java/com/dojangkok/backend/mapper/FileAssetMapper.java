package com.dojangkok.backend.mapper;

import com.dojangkok.backend.domain.FileAsset;
import com.dojangkok.backend.dto.fileasset.FileUploadCompleteItemResponseDto;
import org.springframework.stereotype.Component;

@Component
public class FileAssetMapper {

    public FileUploadCompleteItemResponseDto toFileUploadCompleteItemResponseDto(FileAsset fileAsset, String presignedUrl) {
        return FileUploadCompleteItemResponseDto.builder()
                .fileAssetId(fileAsset.getId())
                .fileKey(fileAsset.getFileKey())
                .fileType(fileAsset.getFileType())
                .status(fileAsset.getStatus())
                .presignedUrl(presignedUrl)
                .build();
    }
}
