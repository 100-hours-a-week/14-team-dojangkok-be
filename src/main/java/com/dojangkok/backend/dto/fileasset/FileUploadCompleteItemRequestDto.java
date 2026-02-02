package com.dojangkok.backend.dto.fileasset;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
public class FileUploadCompleteItemRequestDto {

    @NotNull
    @JsonProperty("file_asset_id")
    private Long fileAssetId;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;
}
