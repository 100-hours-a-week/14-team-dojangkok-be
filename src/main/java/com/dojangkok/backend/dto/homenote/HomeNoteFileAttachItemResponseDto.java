package com.dojangkok.backend.dto.homenote;

import com.dojangkok.backend.domain.enums.FileAssetStatus;
import com.dojangkok.backend.domain.enums.FileType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HomeNoteFileAttachItemResponseDto {

    @JsonProperty("home_note_file_id")
    private Long homeNoteFileId;

    @JsonProperty("file_asset_id")
    private Long fileAssetId;

    @JsonProperty("file_type")
    private FileType fileType;

    @JsonProperty("asset_status")
    private FileAssetStatus assetStatus;
}
