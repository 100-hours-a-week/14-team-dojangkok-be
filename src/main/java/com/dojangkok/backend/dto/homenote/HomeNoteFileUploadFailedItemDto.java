package com.dojangkok.backend.dto.homenote;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HomeNoteFileUploadFailedItemDto {

    @JsonProperty("file_name")
    private String fileName;

    @JsonProperty("size_bytes")
    private Long sizeBytes;

    @JsonProperty("message")
    private String message;

    @JsonProperty("max_size_bytes")
    private Long maxSizeBytes;
}
