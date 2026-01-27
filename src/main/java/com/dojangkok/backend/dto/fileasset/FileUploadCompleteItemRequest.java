package com.dojangkok.backend.dto.fileasset;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
public class FileUploadCompleteItemRequest {

    @NotNull(message = "파일 에셋 ID는 필수입니다.")
    private Long fileAssetId;

    private Long size;

    private Map<String, Object> metadata;
}
