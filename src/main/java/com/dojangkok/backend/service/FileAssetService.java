package com.dojangkok.backend.service;

import com.dojangkok.backend.domain.FileAsset;
import com.dojangkok.backend.dto.fileasset.*;
import com.dojangkok.backend.repository.FileAssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileAssetService {

    private final FileAssetRepository fileAssetRepository;
    private final S3Service s3Service;

    /**
     * 단일 파일에 대해 presigned URL 생성 및 FileAsset 저장
     * @return 생성된 응답 DTO
     */
    @Transactional
    public PresignedUrlItemResponseDto generatePresignedUrlForItem(PresignedUrlItemRequestDto item) {
        String fileKey = generateFileKey(item.getFileType().name(), item.getFileName());

        FileAsset fileAsset = FileAsset.createFileAsset(
                fileKey,
                item.getFileType(),
                item.getFileName(),
                item.getContentType(),
                Map.of("sizeBytes", item.getSizeBytes())
        );
        fileAssetRepository.save(fileAsset);

        String presignedUrl = s3Service.generatePresignedUploadUrl(fileKey, item.getContentType());

        log.info(
                "Generated presigned URL for fileAssetId: {}, fileKey: {}, originalFileName: {}, sizeBytes: {}",
                fileAsset.getId(),
                fileKey,
                item.getFileName(),
                item.getSizeBytes()
        );

        return PresignedUrlItemResponseDto.builder()
                .fileAssetId(fileAsset.getId())
                .presignedUrl(presignedUrl)
                .fileKey(fileKey)
                .build();
    }


    private String generateFileKey(String fileType, String originalFileName) {
        String uuid = UUID.randomUUID().toString();
        String extension = extractExtension(originalFileName);

        return String.format("%s/%s.%s", fileType.toLowerCase(), uuid, extension);
    }

    private String extractExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        if (idx == -1) {
            return ""; // 확장자 없는 파일도 허용
        }
        return fileName.substring(idx + 1).toLowerCase();
    }

}
