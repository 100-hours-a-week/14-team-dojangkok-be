package com.dojangkok.backend.dto.propertypost;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PropertyPostFileUploadFailedItemDto {

    @JsonProperty("file_name")
    private String fileName;

    @JsonProperty("size_bytes")
    private Long sizeBytes;

    @JsonProperty("message")
    private String message;

    @JsonProperty("max_size_bytes")
    private Long maxSizeBytes;
}
