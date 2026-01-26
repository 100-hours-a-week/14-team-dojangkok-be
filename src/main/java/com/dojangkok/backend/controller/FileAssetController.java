package com.dojangkok.backend.controller;

import com.dojangkok.backend.common.dto.DataResponseDto;
import com.dojangkok.backend.common.enums.Code;
import com.dojangkok.backend.dto.fileasset.FileUploadCompleteRequestDto;
import com.dojangkok.backend.dto.fileasset.FileUploadCompleteResponseDto;
import com.dojangkok.backend.dto.fileasset.PresignedUrlRequestDto;
import com.dojangkok.backend.dto.fileasset.PresignedUrlResponseDto;
import com.dojangkok.backend.service.FileAssetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/file-assets")
@RequiredArgsConstructor
public class FileAssetController {

    private final FileAssetService fileAssetService;

    @PostMapping("/presigned-urls")
    public DataResponseDto<PresignedUrlResponseDto> generatePresignedUrls(@Valid @RequestBody PresignedUrlRequestDto presignedUrlRequestDto) {
        PresignedUrlResponseDto response = fileAssetService.generatePresignedUrls(presignedUrlRequestDto);
        return new DataResponseDto<>(Code.SUCCESS, "Presigned URL 발급에 성공하였습니다.", response);
    }

    @PostMapping
    public DataResponseDto<FileUploadCompleteResponseDto> completeFileUpload(@Valid @RequestBody FileUploadCompleteRequestDto request) {
        FileUploadCompleteResponseDto response = fileAssetService.completeFileUpload(request);
        return new DataResponseDto<>(Code.SUCCESS, "파일 업로드 완료 처리에 성공하였습니다.", response);
    }
}
