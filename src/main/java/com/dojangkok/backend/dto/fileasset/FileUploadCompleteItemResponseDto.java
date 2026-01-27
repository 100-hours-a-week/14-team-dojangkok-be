package com.dojangkok.backend.dto.fileasset;

import com.dojangkok.backend.domain.enums.FileAssetStatus;
import com.dojangkok.backend.domain.enums.FileType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileUploadCompleteItemResponseDto {

    @JsonProperty("file_asset_id")
    private Long fileAssetId;

    @JsonProperty("file_key")
    private String fileKey;

    @JsonProperty("file_type")
    private FileType fileType;

    @JsonProperty("file_status")
    private FileAssetStatus status;

    @JsonProperty("presigned_url")
    private String presignedUrl;
}
