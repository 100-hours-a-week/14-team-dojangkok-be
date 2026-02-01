package com.dojangkok.backend.dto.fileasset;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FileSizeExceededErrorDto {

    @JsonProperty("max_size_bytes")
    private Long maxSizeBytes;

    @JsonProperty("size_exceeded_files")
    private List<ExceededFileDto> sizeExceededFiles;

    @Getter
    @Builder
    public static class ExceededFileDto {
        @JsonProperty("file_name")
        private String fileName;

        @JsonProperty("size_bytes")
        private Long sizeBytes;
    }
}
