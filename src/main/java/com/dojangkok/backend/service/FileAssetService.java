package com.dojangkok.backend.service;

import com.dojangkok.backend.common.enums.Code;
import com.dojangkok.backend.common.exception.GeneralException;
import com.dojangkok.backend.domain.FileAsset;
import com.dojangkok.backend.domain.enums.FileAssetStatus;
import com.dojangkok.backend.dto.fileasset.*;
import com.dojangkok.backend.repository.FileAssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileAssetService {

    private final FileAssetRepository fileAssetRepository;
    private final S3Service s3Service;

    @Transactional
    public PresignedUrlResponseDto generatePresignedUrls(PresignedUrlRequestDto request) {
        List<PresignedUrlItemResponseDto> fileItems = new ArrayList<>();

        for (PresignedUrlItemRequestDto item : request.getFileItems()) {
            String fileKey = generateFileKey(item.getFileType().name(), item.getFileName());

            FileAsset fileAsset = FileAsset.createFileAsset(
                    fileKey,
                    item.getFileType(),
                    item.getContentType(),
                    null
            );
            fileAssetRepository.save(fileAsset);

            String presignedUrl = s3Service.generatePresignedUploadUrl(fileKey, item.getContentType());

            fileItems.add(PresignedUrlItemResponseDto.builder()
                    .fileAssetId(fileAsset.getId())
                    .presignedUrl(presignedUrl)
                    .fileKey(fileKey)
                    .build());

            log.info("Generated presigned URL for fileAssetId: {}, fileKey: {}", fileAsset.getId(), fileKey);
        }

        return PresignedUrlResponseDto.builder()
                .fileItems(fileItems)
                .build();
    }

    @Transactional
    public FileUploadCompleteResponseDto completeFileUpload(FileUploadCompleteRequestDto request) {
        List<Long> fileAssetIds = request.getFileItems().stream()
                .map(FileUploadCompleteItemRequestDto::getFileAssetId)
                .toList();

        List<FileAsset> fileAssets = fileAssetRepository.findAllByIdIn(fileAssetIds);

        Map<Long, FileAsset> fileAssetMap = fileAssets.stream()
                .collect(Collectors.toMap(FileAsset::getId, Function.identity()));

        List<FileUploadCompleteItemResponseDto> responseItems = new ArrayList<>();

        for (FileUploadCompleteItemRequestDto item : request.getFileItems()) {
            FileAsset fileAsset = fileAssetMap.get(item.getFileAssetId());

            if (fileAsset == null) {
                throw new GeneralException(Code.FILE_NOT_FOUND);
            }

            if (fileAsset.getStatus() == FileAssetStatus.COMPLETED) {
                log.warn("FileAsset already completed: {}", item.getFileAssetId());
                responseItems.add(buildCompleteItemResponse(fileAsset));
                continue;
            }

            if (!s3Service.doesObjectExist(fileAsset.getFileKey())) {
                throw new GeneralException(Code.FILE_NOT_FOUND);
            }

            if (item.getMetadata() != null) {
                fileAsset.getMetadata().putAll(item.getMetadata());
            }
            if (item.getSize() != null) {
                fileAsset.getMetadata().put("size", item.getSize());
            }

            fileAsset.markCompleted();

            log.info("File upload completed for fileAssetId: {}", item.getFileAssetId());

            responseItems.add(buildCompleteItemResponse(fileAsset));
        }

        return FileUploadCompleteResponseDto.builder()
                .fileItems(responseItems)
                .build();
    }

    private FileUploadCompleteItemResponseDto buildCompleteItemResponse(FileAsset fileAsset) {
        String presignedUrl = s3Service.generatePresignedDownloadUrl(fileAsset.getFileKey());

        return FileUploadCompleteItemResponseDto.builder()
                .fileAssetId(fileAsset.getId())
                .fileKey(fileAsset.getFileKey())
                .fileType(fileAsset.getFileType())
                .status(fileAsset.getStatus())
                .presignedUrl(presignedUrl)
                .build();
    }

    private String generateFileKey(String fileType, String fileName) {
        String uuid = UUID.randomUUID().toString();
        return String.format("%s/%s/%s", fileType.toLowerCase(), uuid, fileName);
    }
}
