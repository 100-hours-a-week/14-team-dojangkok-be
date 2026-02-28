package com.dojangkok.backend.service;

import com.dojangkok.backend.common.enums.Code;
import com.dojangkok.backend.common.exception.GeneralException;
import com.dojangkok.backend.domain.FileAsset;
import com.dojangkok.backend.domain.enums.FileAssetStatus;
import com.dojangkok.backend.dto.fileasset.FileUploadCompleteItemRequestDto;
import com.dojangkok.backend.dto.fileasset.FileUploadCompleteItemResponseDto;
import com.dojangkok.backend.dto.fileasset.FileUploadCompleteRequestDto;
import com.dojangkok.backend.dto.fileasset.PresignedUrlItemRequestDto;
import com.dojangkok.backend.dto.fileasset.PresignedUrlItemResponseDto;
import com.dojangkok.backend.dto.propertypost.*;
import com.dojangkok.backend.mapper.FileAssetMapper;
import com.dojangkok.backend.repository.FileAssetRepository;
import com.dojangkok.backend.repository.PropertyPostFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyPostFileUploadService {

    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10MB
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png"
    );

    private final FileAssetService fileAssetService;
    private final FileAssetRepository fileAssetRepository;
    private final S3Service s3Service;
    private final FileAssetMapper fileAssetMapper;

    @Transactional
    public PropertyPostFileUploadResponseDto generatePresignedUrls(PropertyPostFileUploadRequestDto request) {
        List<PresignedUrlItemResponseDto> successItems = new ArrayList<>();
        List<PropertyPostFileUploadFailedItemDto> failedItems = new ArrayList<>();

        int requestCount = request.getFileItems().size();

        if (requestCount > 50) {
            log.warn("Property post file count exceeded: request={}, max=50", requestCount);
            throw new GeneralException(Code.FILE_COUNT_EXCEEDED);
        }

        for (PresignedUrlItemRequestDto item : request.getFileItems()) {
            // 1. Content-Type 검증
            if (!ALLOWED_CONTENT_TYPES.contains(item.getContentType().toLowerCase())) {
                failedItems.add(PropertyPostFileUploadFailedItemDto.builder()
                        .fileName(item.getFileName())
                        .sizeBytes(item.getSizeBytes())
                        .message("FILE_CONTENT_TYPE_NOT_ALLOWED")
                        .build());
                log.warn("Content type not allowed for property post: fileName={}, contentType={}",
                        item.getFileName(), item.getContentType());
                continue;
            }

            // 2. 용량 검증
            if (item.getSizeBytes() > MAX_FILE_SIZE_BYTES) {
                failedItems.add(PropertyPostFileUploadFailedItemDto.builder()
                        .fileName(item.getFileName())
                        .sizeBytes(item.getSizeBytes())
                        .message("FILE_SIZE_EXCEEDED")
                        .maxSizeBytes(MAX_FILE_SIZE_BYTES)
                        .build());
                log.warn("File size exceeded for property post: fileName={}, sizeBytes={}, maxSizeBytes={}",
                        item.getFileName(), item.getSizeBytes(), MAX_FILE_SIZE_BYTES);
                continue;
            }

            // 3. 검증 통과 - presigned URL 생성
            PresignedUrlItemResponseDto responseItem = fileAssetService.generatePresignedUrlForItem(item, "propertypost");
            successItems.add(responseItem);
        }

        log.info("Property post file upload prepared: successCount={}, failedCount={}",
                successItems.size(), failedItems.size());

        return PropertyPostFileUploadResponseDto.builder()
                .successFileItems(successItems)
                .failedFileItems(failedItems)
                .build();
    }

    /**
     * 매물 게시글 파일 업로드 완료 검증 (부분 실패 허용)
     */
    @Transactional
    public PropertyPostFileCompleteResponseDto completeFileUpload(FileUploadCompleteRequestDto request) {
        List<Long> fileAssetIds = request.getFileItems().stream()
                .map(FileUploadCompleteItemRequestDto::getFileAssetId)
                .toList();

        List<FileAsset> fileAssets = fileAssetRepository.findAllByIdIn(fileAssetIds);
        Map<Long, FileAsset> fileAssetMap = fileAssets.stream()
                .collect(Collectors.toMap(FileAsset::getId, Function.identity()));

        List<FileUploadCompleteItemResponseDto> successItems = new ArrayList<>();
        List<PropertyPostFileCompleteFailedItemDto> failedItems = new ArrayList<>();

        for (FileUploadCompleteItemRequestDto item : request.getFileItems()) {
            FileAsset fileAsset = fileAssetMap.get(item.getFileAssetId());

            // 1. file_asset_id 존재 여부 검증
            if (fileAsset == null) {
                failedItems.add(PropertyPostFileCompleteFailedItemDto.builder()
                        .fileAssetId(item.getFileAssetId())
                        .message("FILE_NOT_FOUND")
                        .build());
                log.warn("FileAsset not found: fileAssetId={}", item.getFileAssetId());
                continue;
            }

            // 이미 완료된 경우 스킵
            if (fileAsset.getStatus() == FileAssetStatus.COMPLETED) {
                log.warn("FileAsset already completed: fileAssetId={}", item.getFileAssetId());
                String presignedUrl = s3Service.generatePresignedDownloadUrl(fileAsset.getFileKey());
                successItems.add(fileAssetMapper.toFileUploadCompleteItemResponseDto(fileAsset, presignedUrl));
                continue;
            }

            // 2. S3 HEAD Object로 검증
            Optional<HeadObjectResponse> headResponse = s3Service.getObjectMetadata(fileAsset.getFileKey());

            if (headResponse.isEmpty()) {
                fileAsset.markFailed("FILE_UPLOAD_NOT_COMPLETED");
                failedItems.add(PropertyPostFileCompleteFailedItemDto.builder()
                        .fileAssetId(fileAsset.getId())
                        .fileKey(fileAsset.getFileKey())
                        .message("FILE_UPLOAD_NOT_COMPLETED")
                        .build());
                log.warn("S3 object not found: fileAssetId={}, fileKey={}", fileAsset.getId(), fileAsset.getFileKey());
                continue;
            }

            HeadObjectResponse head = headResponse.get();
            long actualSize = head.contentLength();
            String actualContentType = head.contentType();

            // 3. 용량 정책 검증
            if (actualSize > MAX_FILE_SIZE_BYTES) {
                s3Service.deleteObject(fileAsset.getFileKey());
                fileAsset.markFailed("FILE_SIZE_EXCEEDED");
                failedItems.add(PropertyPostFileCompleteFailedItemDto.builder()
                        .fileAssetId(fileAsset.getId())
                        .fileKey(fileAsset.getFileKey())
                        .message("FILE_SIZE_EXCEEDED")
                        .build());
                log.warn("Actual file size exceeded: fileAssetId={}, actualSize={}", fileAsset.getId(), actualSize);
                continue;
            }

            // 4. Content-Type 검증
            if (!ALLOWED_CONTENT_TYPES.contains(actualContentType.toLowerCase())) {
                s3Service.deleteObject(fileAsset.getFileKey());
                fileAsset.markFailed("FILE_CONTENT_TYPE_NOT_ALLOWED");
                failedItems.add(PropertyPostFileCompleteFailedItemDto.builder()
                        .fileAssetId(fileAsset.getId())
                        .fileKey(fileAsset.getFileKey())
                        .message("FILE_CONTENT_TYPE_NOT_ALLOWED")
                        .build());
                log.warn("Actual content type not allowed: fileAssetId={}, actualContentType={}", fileAsset.getId(), actualContentType);
                continue;
            }

            // 5. 위변조 검증
            Object raw = fileAsset.getMetadata().get("sizeBytes");
            Long declaredSize = raw == null ? null : ((Number) raw).longValue();
            String declaredContentType = fileAsset.getContentType();

            if (declaredSize != null && !declaredSize.equals(actualSize)) {
                s3Service.deleteObject(fileAsset.getFileKey());
                fileAsset.markFailed("FILE_SIZE_MISMATCH");
                failedItems.add(PropertyPostFileCompleteFailedItemDto.builder()
                        .fileAssetId(fileAsset.getId())
                        .fileKey(fileAsset.getFileKey())
                        .message("FILE_SIZE_MISMATCH")
                        .build());
                log.warn("File size mismatch: fileAssetId={}, declared={}, actual={}", fileAsset.getId(), declaredSize, actualSize);
                continue;
            }

            if (!declaredContentType.equalsIgnoreCase(actualContentType)) {
                s3Service.deleteObject(fileAsset.getFileKey());
                fileAsset.markFailed("FILE_CONTENT_TYPE_MISMATCH");
                failedItems.add(PropertyPostFileCompleteFailedItemDto.builder()
                        .fileAssetId(fileAsset.getId())
                        .fileKey(fileAsset.getFileKey())
                        .message("FILE_CONTENT_TYPE_MISMATCH")
                        .build());
                log.warn("Content type mismatch: fileAssetId={}, declared={}, actual={}", fileAsset.getId(), declaredContentType, actualContentType);
                continue;
            }

            // 6. 모든 검증 통과 - 완료 처리
            fileAsset.markCompleted();
            String presignedUrl = s3Service.generatePresignedDownloadUrl(fileAsset.getFileKey());
            successItems.add(fileAssetMapper.toFileUploadCompleteItemResponseDto(fileAsset, presignedUrl));
            log.info("File upload completed for fileAssetId: {}", fileAsset.getId());
        }

        log.info("Property post file upload complete: successCount={}, failedCount={}", successItems.size(), failedItems.size());

        return PropertyPostFileCompleteResponseDto.builder()
                .successItems(successItems)
                .failedItems(failedItems)
                .build();
    }
}
