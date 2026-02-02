package com.dojangkok.backend.dto.easycontract;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class EasyContractFileCompleteErrorDto {

    @JsonProperty("failed_files")
    private List<FailedFileDto> failedFiles;

    @Getter
    @Builder
    public static class FailedFileDto {
        @JsonProperty("file_asset_id")
        private Long fileAssetId;

        @JsonProperty("file_key")
        private String fileKey;

        @JsonProperty("message")
        private String message;
    }
}
