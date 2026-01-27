package com.dojangkok.backend.dto.homenote;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PreviewImageDto {

    @JsonProperty("file_asset_id")
    private Long fileAssetId;

    @JsonProperty("presigned_url")
    private String presignedUrl;
}
