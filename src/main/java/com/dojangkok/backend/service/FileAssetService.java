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
        long startTime = System.currentTimeMillis();
        
        String fileKey = generateFileKey(item.getFileType().name(), item.getFileName());

        FileAsset fileAsset = FileAsset.createFileAsset(
                fileKey,
                item.getFileType(),
                item.getFileName(),
                item.getContentType(),
                Map.of("sizeBytes", item.getSizeBytes())
        );
        
        // [TIMING] DB 저장
        long dbSaveStart = System.currentTimeMillis();
        fileAssetRepository.save(fileAsset);
        long dbSaveDuration = System.currentTimeMillis() - dbSaveStart;

        // [TIMING] Presigned URL 생성
        long presignedStart = System.currentTimeMillis();
        String presignedUrl = s3Service.generatePresignedUploadUrl(fileKey, item.getContentType());
        long presignedDuration = System.currentTimeMillis() - presignedStart;
        
        long totalDuration = System.currentTimeMillis() - startTime;
        
        // 전체가 30ms 이상이면 상세 로그
        if (totalDuration > 30) {
            log.info("[TIMING] FileAsset 생성 상세 - 총: {}ms (DB저장: {}ms, presigned생성: {}ms) fileName={}",
                    totalDuration, dbSaveDuration, presignedDuration, item.getFileName());
        }

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
