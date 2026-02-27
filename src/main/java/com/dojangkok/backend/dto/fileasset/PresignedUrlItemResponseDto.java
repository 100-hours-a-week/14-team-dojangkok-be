package com.dojangkok.backend.dto.fileasset;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import software.amazon.awssdk.annotations.NotNull;

@Getter
@Builder
public class PresignedUrlItemResponseDto {

    @JsonProperty("file_asset_id")
    private Long fileAssetId;

    @JsonProperty("presigned_url")
    private String presignedUrl;

    @JsonProperty("file_key")
    private String fileKey;
}
