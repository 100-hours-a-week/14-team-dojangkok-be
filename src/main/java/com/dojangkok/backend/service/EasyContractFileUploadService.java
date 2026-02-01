package com.dojangkok.backend.service;

import com.dojangkok.backend.common.enums.Code;
import com.dojangkok.backend.common.exception.GeneralException;
import com.dojangkok.backend.domain.FileAsset;
import com.dojangkok.backend.domain.enums.FileAssetStatus;
import com.dojangkok.backend.dto.easycontract.EasyContractFileCompleteErrorDto;
import com.dojangkok.backend.dto.easycontract.EasyContractFileCompleteResponseDto;
import com.dojangkok.backend.dto.easycontract.EasyContractFileUploadRequestDto;
import com.dojangkok.backend.dto.easycontract.EasyContractFileUploadResponseDto;
import com.dojangkok.backend.dto.fileasset.*;
import com.dojangkok.backend.mapper.FileAssetMapper;
import com.dojangkok.backend.repository.FileAssetRepository;
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
public class EasyContractFileUploadService {

    private static final long MAX_FILE_SIZE_BYTES = 15 * 1024 * 1024; // 15MB
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "application/pdf"
    );

    private final FileAssetService fileAssetService;
    private final FileAssetRepository fileAssetRepository;
    private final S3Service s3Service;
    private final FileAssetMapper fileAssetMapper;

    /**
     * 쉬운 계약서 파일 업로드 준비 (전체 실패)
     * - 하나라도 정책 위반 시 전체 실패 (예외 발생)
     */
    @Transactional
    public EasyContractFileUploadResponseDto generatePresignedUrls(EasyContractFileUploadRequestDto request) {
        List<FileSizeExceededErrorDto.ExceededFileDto> exceededFiles = new ArrayList<>();
        List<String> notAllowedContentTypeFiles = new ArrayList<>();

        for (PresignedUrlItemRequestDto item : request.getFileItems()) {
            // 1. Content-Type 검증
            if (!ALLOWED_CONTENT_TYPES.contains(item.getContentType().toLowerCase())) {
                notAllowedContentTypeFiles.add(item.getFileName());
            }

            // 2. 용량 검증
            if (item.getSizeBytes() > MAX_FILE_SIZE_BYTES) {
                exceededFiles.add(FileSizeExceededErrorDto.ExceededFileDto.builder()
                        .fileName(item.getFileName())
                        .sizeBytes(item.getSizeBytes())
                        .build());
            }
        }

        // Content-Type 위반 시 전체 실패
        if (!notAllowedContentTypeFiles.isEmpty()) {
            log.warn("Content type not allowed for easy contract: files={}", notAllowedContentTypeFiles);
            throw new GeneralException(Code.FILE_CONTENT_TYPE_NOT_ALLOWED,
                    Map.of("notAllowedFiles", notAllowedContentTypeFiles,
                            "allowedContentTypes", ALLOWED_CONTENT_TYPES));
        }

        // 용량 초과 시 전체 실패
        if (!exceededFiles.isEmpty()) {
            log.warn("File size exceeded for easy contract: exceededFileCount={}", exceededFiles.size());

            FileSizeExceededErrorDto errorData = FileSizeExceededErrorDto.builder()
                    .maxSizeBytes(MAX_FILE_SIZE_BYTES)
                    .sizeExceededFiles(exceededFiles)
                    .build();

            throw new GeneralException(Code.FILE_SIZE_EXCEEDED, errorData);
        }

        // 3. 모든 파일 검증 통과 - presigned URL 생성
        List<PresignedUrlItemResponseDto> fileItems = new ArrayList<>();
        for (PresignedUrlItemRequestDto item : request.getFileItems()) {
            PresignedUrlItemResponseDto responseItem = fileAssetService.generatePresignedUrlForItem(item);
            fileItems.add(responseItem);
        }

        log.info("Easy contract file upload prepared: fileCount={}", fileItems.size());

        return EasyContractFileUploadResponseDto.builder()
                .fileItems(fileItems)
                .build();
    }

    /**
     * 쉬운 계약서 파일 업로드 완료 검증 (전체 실패)
     * - 하나라도 검증 실패 시 전체 실패 (예외 발생)
     * - S3 존재 여부, 용량/타입 위변조 검증
     */
    @Transactional
    public EasyContractFileCompleteResponseDto completeFileUpload(FileUploadCompleteRequestDto request) {
        List<Long> fileAssetIds = request.getFileItems().stream()
                .map(FileUploadCompleteItemRequestDto::getFileAssetId)
                .toList();

        List<FileAsset> fileAssets = fileAssetRepository.findAllByIdIn(fileAssetIds);
        Map<Long, FileAsset> fileAssetMap = fileAssets.stream()
                .collect(Collectors.toMap(FileAsset::getId, Function.identity()));

        List<FileUploadCompleteItemResponseDto> successItems = new ArrayList<>();
        List<EasyContractFileCompleteErrorDto.FailedFileDto> failedItems = new ArrayList<>();

        for (FileUploadCompleteItemRequestDto item : request.getFileItems()) {
            FileAsset fileAsset = fileAssetMap.get(item.getFileAssetId());

            // 1. file_asset_id 존재 여부 검증
            if (fileAsset == null) {
                failedItems.add(EasyContractFileCompleteErrorDto.FailedFileDto.builder()
                        .fileAssetId(item.getFileAssetId())
                        .message("FILE_NOT_FOUND")
                        .build());
                log.warn("FileAsset not found: fileAssetId={}", item.getFileAssetId());
                continue;
            }

            // 이미 완료된 경우 성공 목록에 추가
            if (fileAsset.getStatus() == FileAssetStatus.COMPLETED) {
                log.info("FileAsset already completed: fileAssetId={}", item.getFileAssetId());
                String presignedUrl = s3Service.generatePresignedDownloadUrl(fileAsset.getFileKey());
                successItems.add(fileAssetMapper.toFileUploadCompleteItemResponseDto(fileAsset, presignedUrl));
                continue;
            }

            // 2. S3 HEAD Object로 검증
            Optional<HeadObjectResponse> headResponse = s3Service.getObjectMetadata(fileAsset.getFileKey());

            if (headResponse.isEmpty()) {
                fileAsset.markFailed("FILE_UPLOAD_NOT_COMPLETED");
                failedItems.add(EasyContractFileCompleteErrorDto.FailedFileDto.builder()
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
                failedItems.add(EasyContractFileCompleteErrorDto.FailedFileDto.builder()
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
                failedItems.add(EasyContractFileCompleteErrorDto.FailedFileDto.builder()
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
                failedItems.add(EasyContractFileCompleteErrorDto.FailedFileDto.builder()
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
                failedItems.add(EasyContractFileCompleteErrorDto.FailedFileDto.builder()
                        .fileAssetId(fileAsset.getId())
                        .fileKey(fileAsset.getFileKey())
                        .message("FILE_CONTENT_TYPE_MISMATCH")
                        .build());
                log.warn("Content type mismatch: fileAssetId={}, declared={}, actual={}", fileAsset.getId(), declaredContentType, actualContentType);
                continue;
            }

            // 6. 검증 통과 - 완료 처리
            fileAsset.markCompleted();
            String presignedUrl = s3Service.generatePresignedDownloadUrl(fileAsset.getFileKey());
            successItems.add(fileAssetMapper.toFileUploadCompleteItemResponseDto(fileAsset, presignedUrl));
            log.info("File upload completed for fileAssetId: {}", fileAsset.getId());
        }

        // 하나라도 실패 시 전체 실패 (예외 발생)
        if (!failedItems.isEmpty()) {
            log.warn("Easy contract file complete failed: failedCount={}", failedItems.size());

            EasyContractFileCompleteErrorDto errorData = EasyContractFileCompleteErrorDto.builder()
                    .failedFiles(failedItems)
                    .build();

            throw new GeneralException(Code.FILE_UPLOAD_NOT_COMPLETED, errorData);
        }

        log.info("Easy contract file upload complete: successCount={}", successItems.size());

        return EasyContractFileCompleteResponseDto.builder()
                .fileItems(successItems)
                .build();
    }
}
