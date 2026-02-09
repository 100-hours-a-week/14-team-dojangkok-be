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
import com.dojangkok.backend.mapper.FileAssetMapper;
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
    private final FileAssetMapper fileAssetMapper;

    /**
     * 집 노트 파일 업로드 준비 (부분 실패 허용)
     * - 용량 초과 또는 허용되지 않는 Content-Type은 제외하고 나머지만 처리
     */
    @Transactional
    public HomeNoteFileUploadResponseDto generatePresignedUrls(Long homeNoteId, HomeNoteFileUploadRequestDto request) {
        long totalStartTime = System.currentTimeMillis();
        log.info("[TIMING] ===== 업로드 준비 API 시작 =====");
        
        List<PresignedUrlItemResponseDto> successItems = new ArrayList<>();
        List<HomeNoteFileUploadFailedItemDto> failedItems = new ArrayList<>();

        int requestCount = request.getFileItems().size();
        
        // [TIMING] 파일 개수 조회
        long countStartTime = System.currentTimeMillis();
        long existingCount = homeNoteFileRepository.countByHomeNoteId(homeNoteId);
        long countDuration = System.currentTimeMillis() - countStartTime;
        log.info("[TIMING] 파일 개수 조회: {}ms (homeNoteId={}, existingCount={})", 
                countDuration, homeNoteId, existingCount);

        if (existingCount + requestCount > 50) {
            log.warn("Home note file count exceeded: homeNoteId={}, existing={}, request={}, max=50",
                    homeNoteId, existingCount, requestCount);

            throw new GeneralException(Code.FILE_COUNT_EXCEEDED);
        }

        // [TIMING] 검증 단계
        long validationStartTime = System.currentTimeMillis();
        List<PresignedUrlItemRequestDto> validItems = new ArrayList<>();
        
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

            validItems.add(item);
        }
        long validationDuration = System.currentTimeMillis() - validationStartTime;
        log.info("[TIMING] 검증 단계: {}ms (validCount={}, failedCount={})", 
                validationDuration, validItems.size(), failedItems.size());

        // [TIMING] Presigned URL 생성 + DB 저장
        long presignedTotalStartTime = System.currentTimeMillis();
        
        for (PresignedUrlItemRequestDto item : validItems) {
            long itemStartTime = System.currentTimeMillis();
            PresignedUrlItemResponseDto responseItem = fileAssetService.generatePresignedUrlForItem(item);
            successItems.add(responseItem);
            long itemDuration = System.currentTimeMillis() - itemStartTime;
            
            // 개별 아이템이 50ms 이상 걸리면 로그
            if (itemDuration > 50) {
                log.warn("[TIMING] 개별 presigned URL 생성 느림: {}ms (fileName={})", 
                        itemDuration, item.getFileName());
            }
        }
        long presignedTotalDuration = System.currentTimeMillis() - presignedTotalStartTime;
        log.info("[TIMING] Presigned URL 생성 + DB 저장 총합: {}ms ({}개)", 
                presignedTotalDuration, validItems.size());

        // [TIMING] 응답 DTO 매핑
        long mappingStartTime = System.currentTimeMillis();
        HomeNoteFileUploadResponseDto response = HomeNoteFileUploadResponseDto.builder()
                .successFileItems(successItems)
                .failedFileItems(failedItems)
                .build();
        long mappingDuration = System.currentTimeMillis() - mappingStartTime;
        log.info("[TIMING] 응답 DTO 매핑: {}ms", mappingDuration);

        // [TIMING] 총합
        long totalDuration = System.currentTimeMillis() - totalStartTime;
        log.info("[TIMING] ===== 업로드 준비 API 총합: {}ms =====", totalDuration);
        log.info("[TIMING] 상세 내역 - 개수조회: {}ms, 검증: {}ms, presigned+DB: {}ms, 매핑: {}ms",
                countDuration, validationDuration, presignedTotalDuration, mappingDuration);

        log.info("Home note file upload prepared: successCount={}, failedCount={}",
                successItems.size(), failedItems.size());

        return response;
    }

    /**
     * 집 노트 파일 업로드 완료 검증 (부분 실패 허용)
     * - S3 존재 여부, 용량/타입 위변조 검증
     * - S3 HEAD 요청은 병렬 처리로 성능 최적화
     */
    @Transactional
    public HomeNoteFileCompleteResponseDto completeFileUpload(FileUploadCompleteRequestDto request) {
        long totalStartTime = System.currentTimeMillis();
        log.info("[TIMING] ===== 업로드 완료 API 시작 =====");
        
        // [TIMING] FileAsset ID 추출
        long extractStartTime = System.currentTimeMillis();
        List<Long> fileAssetIds = request.getFileItems().stream()
                .map(FileUploadCompleteItemRequestDto::getFileAssetId)
                .toList();
        long extractDuration = System.currentTimeMillis() - extractStartTime;
        log.info("[TIMING] FileAsset ID 추출: {}ms ({}개)", extractDuration, fileAssetIds.size());

        // [TIMING] FileAsset 조회 (DB)
        long dbQueryStartTime = System.currentTimeMillis();
        List<FileAsset> fileAssets = fileAssetRepository.findAllByIdIn(fileAssetIds);
        long dbQueryDuration = System.currentTimeMillis() - dbQueryStartTime;
        log.info("[TIMING] FileAsset DB 조회: {}ms ({}개)", dbQueryDuration, fileAssets.size());
        
        Map<Long, FileAsset> fileAssetMap = fileAssets.stream()
                .collect(Collectors.toMap(FileAsset::getId, Function.identity()));

        // HEAD 요청이 필요한 fileAssetId 목록 (존재하고, 아직 완료되지 않은 것만)
        List<Long> fileAssetIdsNeedingHead = request.getFileItems().stream()
                .map(FileUploadCompleteItemRequestDto::getFileAssetId)
                .filter(id -> {
                    FileAsset fileAsset = fileAssetMap.get(id);
                    return fileAsset != null && fileAsset.getStatus() != FileAssetStatus.COMPLETED;
                })
                .toList();
        
        // [TIMING] S3 HEAD 요청 병렬 처리
        long s3HeadStartTime = System.currentTimeMillis();
        Map<Long, Optional<HeadObjectResponse>> headResponses = fileAssetIdsNeedingHead.parallelStream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> {
                            FileAsset fileAsset = fileAssetMap.get(id);
                            return s3Service.getObjectMetadata(fileAsset.getFileKey());
                        }
                ));
        long s3HeadDuration = System.currentTimeMillis() - s3HeadStartTime;
        log.info("[TIMING] S3 HEAD 총합: {}ms ({}개)", s3HeadDuration, fileAssetIdsNeedingHead.size());

        List<FileUploadCompleteItemResponseDto> successItems = new ArrayList<>();
        List<HomeNoteFileCompleteFailedItemDto> failedItems = new ArrayList<>();

        // [TIMING] 검증 및 후처리
        long validationLoopStartTime = System.currentTimeMillis();
        long s3DeleteTotalTime = 0;
        long presignedDownloadTotalTime = 0;
        int s3DeleteCount = 0;
        
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
                long presignedStart = System.currentTimeMillis();
                String presignedUrl = s3Service.generatePresignedDownloadUrl(fileAsset.getFileKey());
                presignedDownloadTotalTime += System.currentTimeMillis() - presignedStart;
                successItems.add(fileAssetMapper.toFileUploadCompleteItemResponseDto(fileAsset, presignedUrl));
                continue;
            }

            // 2. 미리 병렬로 조회한 S3 HEAD 응답 사용
            Optional<HeadObjectResponse> headResponse = headResponses.get(item.getFileAssetId());

            if (headResponse == null || headResponse.isEmpty()) {
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
                long deleteStart = System.currentTimeMillis();
                s3Service.deleteObject(fileAsset.getFileKey());
                s3DeleteTotalTime += System.currentTimeMillis() - deleteStart;
                s3DeleteCount++;
                
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
                long deleteStart = System.currentTimeMillis();
                s3Service.deleteObject(fileAsset.getFileKey());
                s3DeleteTotalTime += System.currentTimeMillis() - deleteStart;
                s3DeleteCount++;
                
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
                long deleteStart = System.currentTimeMillis();
                s3Service.deleteObject(fileAsset.getFileKey());
                s3DeleteTotalTime += System.currentTimeMillis() - deleteStart;
                s3DeleteCount++;
                
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
                long deleteStart = System.currentTimeMillis();
                s3Service.deleteObject(fileAsset.getFileKey());
                s3DeleteTotalTime += System.currentTimeMillis() - deleteStart;
                s3DeleteCount++;
                
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
            
            long presignedStart = System.currentTimeMillis();
            String presignedUrl = s3Service.generatePresignedDownloadUrl(fileAsset.getFileKey());
            presignedDownloadTotalTime += System.currentTimeMillis() - presignedStart;
            
            successItems.add(fileAssetMapper.toFileUploadCompleteItemResponseDto(fileAsset, presignedUrl));
            log.info("File upload completed for fileAssetId: {}", fileAsset.getId());
        }
        
        long validationLoopDuration = System.currentTimeMillis() - validationLoopStartTime;
        long etcValidationTime = validationLoopDuration - presignedDownloadTotalTime - s3DeleteTotalTime;
        
        // [TIMING] 상세 로그
        log.info("[TIMING] Presigned Download URL 생성 총합: {}ms", presignedDownloadTotalTime);
        log.info("[TIMING] 기타 검증: {}ms", etcValidationTime);
        log.info("[TIMING] 검증 루프 전체: {}ms", validationLoopDuration);
        if (s3DeleteCount > 0) {
            log.info("[TIMING] S3 DELETE 총합: {}ms ({}개)", s3DeleteTotalTime, s3DeleteCount);
        }

        // [TIMING] 총합
        long totalDuration = System.currentTimeMillis() - totalStartTime;
        log.info("[TIMING] ===== 업로드 완료 API 총합: {}ms =====", totalDuration);
        log.info("[TIMING] 상세 내역 - DB조회: {}ms, S3 HEAD: {}ms, presigned: {}ms, 기타검증: {}ms",
                dbQueryDuration, s3HeadDuration, presignedDownloadTotalTime, etcValidationTime);

        log.info("Home note file upload complete: successCount={}, failedCount={}", successItems.size(), failedItems.size());

        return HomeNoteFileCompleteResponseDto.builder()
                .successItems(successItems)
                .failedItems(failedItems)
                .build();
    }

}
