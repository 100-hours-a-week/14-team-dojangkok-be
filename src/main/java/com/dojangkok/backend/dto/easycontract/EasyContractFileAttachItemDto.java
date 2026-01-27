package com.dojangkok.backend.dto.easycontract;

import com.dojangkok.backend.domain.enums.FileAssetStatus;
import com.dojangkok.backend.domain.enums.FileType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EasyContractFileAttachItemDto {

    @JsonProperty("easy_contract_file_id")
    private Long easyContractFileId;

    @JsonProperty("file_asset_id")
    private Long fileAssetId;

    @JsonProperty("file_type")
    private FileType fileType;

    @JsonProperty("asset_status")
    private FileAssetStatus assetStatus;
}
