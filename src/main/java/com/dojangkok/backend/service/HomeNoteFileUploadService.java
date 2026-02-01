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
import com.dojangkok.backend.dto.homenote.HomeNoteFileCompleteFailedItemDto;
import com.dojangkok.backend.dto.homenote.HomeNoteFileCompleteResponseDto;
import com.dojangkok.backend.dto.homenote.HomeNoteFileUploadFailedItemDto;
import com.dojangkok.backend.dto.homenote.HomeNoteFileUploadRequestDto;
import com.dojangkok.backend.dto.homenote.HomeNoteFileUploadResponseDto;
import com.dojangkok.backend.mapper.HomeNoteMapper;
import com.dojangkok.backend.repository.FileAssetRepository;
import com.dojangkok.backend.repository.HomeNoteFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class HomeNoteFileUploadService {

    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10MB
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png"
    );

    private final FileAssetService fileAssetService;
    private final FileAssetRepository fileAssetRepository;
    private final HomeNoteFileRepository homeNoteFileRepository;
    private final S3Service s3Service;
    private final HomeNoteMapper homeNoteMapper;

    /**
     * 집 노트 파일 업로드 준비 (부분 실패 허용)
     * - 용량 초과 또는 허용되지 않는 Content-Type은 제외하고 나머지만 처리
     */
    @Transactional
    public HomeNoteFileUploadResponseDto generatePresignedUrls(Long homeNoteId, HomeNoteFileUploadRequestDto request) {
        List<PresignedUrlItemResponseDto> successItems = new ArrayList<>();
        List<HomeNoteFileUploadFailedItemDto> failedItems = new ArrayList<>();

        int requestCount = request.getFileItems().size();
        long existingCount = homeNoteFileRepository.countByHomeNoteId(homeNoteId);

        if (existingCount + requestCount > 50) {
            log.warn("Home note file count exceeded: homeNoteId={}, existing={}, request={}, max=50",
                    homeNoteId, existingCount, requestCount);

            throw new GeneralException(Code.FILE_COUNT_EXCEEDED);
        }

        for (PresignedUrlItemRequestDto item : request.getFileItems()) {
            // 1. Content-Type 검증
            if (!ALLOWED_CONTENT_TYPES.contains(item.getContentType().toLowerCase())) {
                failedItems.add(HomeNoteFileUploadFailedItemDto.builder()
                        .fileName(item.getFileName())
                        .sizeBytes(item.getSizeBytes())
                        .message("FILE_CONTENT_TYPE_NOT_ALLOWED")
                        .build());
                log.warn("Content type not allowed for home note: fileName={}, contentType={}",
                        item.getFileName(), item.getContentType());
                continue;
            }

            // 2. 용량 검증
            if (item.getSizeBytes() > MAX_FILE_SIZE_BYTES) {
                failedItems.add(HomeNoteFileUploadFailedItemDto.builder()
                        .fileName(item.getFileName())
                        .sizeBytes(item.getSizeBytes())
                        .message("FILE_SIZE_EXCEEDED")
                        .maxSizeBytes(MAX_FILE_SIZE_BYTES)
                        .build());
                log.warn("File size exceeded for home note: fileName={}, sizeBytes={}, maxSizeBytes={}",
                        item.getFileName(), item.getSizeBytes(), MAX_FILE_SIZE_BYTES);
                continue;
            }

            // 3. 검증 통과 - presigned URL 생성
            PresignedUrlItemResponseDto responseItem = fileAssetService.generatePresignedUrlForItem(item);
            successItems.add(responseItem);
        }

        log.info("Home note file upload prepared: successCount={}, failedCount={}",
                successItems.size(), failedItems.size());

        return HomeNoteFileUploadResponseDto.builder()
                .successFileItems(successItems)
                .failedFileItems(failedItems)
                .build();
    }

    /**
     * 집 노트 파일 업로드 완료 검증 (부분 실패 허용)
     * - S3 존재 여부, 용량/타입 위변조 검증
     */
    @Transactional
    public HomeNoteFileCompleteResponseDto completeFileUpload(FileUploadCompleteRequestDto request) {
        List<Long> fileAssetIds = request.getFileItems().stream()
                .map(FileUploadCompleteItemRequestDto::getFileAssetId)
                .toList();

        List<FileAsset> fileAssets = fileAssetRepository.findAllByIdIn(fileAssetIds);
        Map<Long, FileAsset> fileAssetMap = fileAssets.stream()
                .collect(Collectors.toMap(FileAsset::getId, Function.identity()));

        List<FileUploadCompleteItemResponseDto> successItems = new ArrayList<>();
        List<HomeNoteFileCompleteFailedItemDto> failedItems = new ArrayList<>();

        for (FileUploadCompleteItemRequestDto item : request.getFileItems()) {
            FileAsset fileAsset = fileAssetMap.get(item.getFileAssetId());

            // 1. file_asset_id 존재 여부 검증
            if (fileAsset == null) {
                failedItems.add(HomeNoteFileCompleteFailedItemDto.builder()
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
                successItems.add(homeNoteMapper.toFileUploadCompleteItemResponseDto(fileAsset, presignedUrl));
                continue;
            }

            // 2. S3 HEAD Object로 검증
            Optional<HeadObjectResponse> headResponse = s3Service.getObjectMetadata(fileAsset.getFileKey());

            if (headResponse.isEmpty()) {
                // S3에 파일이 없음
                fileAsset.markFailed("FILE_UPLOAD_NOT_COMPLETED");
                failedItems.add(HomeNoteFileCompleteFailedItemDto.builder()
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
                failedItems.add(HomeNoteFileCompleteFailedItemDto.builder()
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
                failedItems.add(HomeNoteFileCompleteFailedItemDto.builder()
                        .fileAssetId(fileAsset.getId())
                        .fileKey(fileAsset.getFileKey())
                        .message("FILE_CONTENT_TYPE_NOT_ALLOWED")
                        .build());
                log.warn("Actual content type not allowed: fileAssetId={}, actualContentType={}", fileAsset.getId(), actualContentType);
                continue;
            }

            // 5. 위변조 검증 - 요청된 값과 실제 값 비교
            Object raw = fileAsset.getMetadata().get("sizeBytes");
            Long declaredSize = raw == null ? null : ((Number) raw).longValue();
            String declaredContentType = fileAsset.getContentType();

            if (declaredSize != null && !declaredSize.equals(actualSize)) {
                s3Service.deleteObject(fileAsset.getFileKey());
                fileAsset.markFailed("FILE_SIZE_MISMATCH");
                failedItems.add(HomeNoteFileCompleteFailedItemDto.builder()
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
                failedItems.add(HomeNoteFileCompleteFailedItemDto.builder()
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
            successItems.add(homeNoteMapper.toFileUploadCompleteItemResponseDto(fileAsset, presignedUrl));
            log.info("File upload completed for fileAssetId: {}", fileAsset.getId());
        }

        log.info("Home note file upload complete: successCount={}, failedCount={}", successItems.size(), failedItems.size());

        return HomeNoteFileCompleteResponseDto.builder()
                .successItems(successItems)
                .failedItems(failedItems)
                .build();
    }

}
