package com.dojangkok.backend.dto.homenote;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HomeNoteFileCompleteFailedItemDto {

    @JsonProperty("file_asset_id")
    private Long fileAssetId;

    @JsonProperty("file_key")
    private String fileKey;

    @JsonProperty("message")
    private String message;
}
