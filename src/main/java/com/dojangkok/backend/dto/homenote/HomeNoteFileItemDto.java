package com.dojangkok.backend.dto.homenote;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class HomeNoteFileItemDto {

    @JsonProperty("home_note_file_id")
    private Long homeNoteFileId;

    @JsonProperty("file_asset_id")
    private Long fileAssetId;

    @JsonProperty("sort_order")
    private int sortOrder;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("presigned_url")
    private String presignedUrl;
}
