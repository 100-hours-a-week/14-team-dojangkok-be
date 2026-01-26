package com.dojangkok.backend.dto.easycontract;

import com.dojangkok.backend.domain.enums.FileAssetStatus;
import com.dojangkok.backend.domain.enums.FileType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class EasyContractAssetItemDto {

    @JsonProperty("easy_contract_file_id")
    private Long easyContractFileId;

    @JsonProperty("file_asset_id")
    private Long fileAssetId;

    @JsonProperty("file_type")
    private FileType fileType;

    @JsonProperty("asset_status")
    private FileAssetStatus assetStatus;

    @JsonProperty("sort_order")
    private int sortOrder;

    @JsonProperty("presigned_url")
    private String presignedUrl;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
