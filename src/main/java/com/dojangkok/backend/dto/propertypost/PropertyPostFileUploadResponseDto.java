package com.dojangkok.backend.dto.propertypost;

import com.dojangkok.backend.dto.fileasset.PresignedUrlItemResponseDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PropertyPostFileUploadResponseDto {

    @JsonProperty("success_file_items")
    private List<PresignedUrlItemResponseDto> successFileItems;

    @JsonProperty("failed_file_items")
    private List<PropertyPostFileUploadFailedItemDto> failedFileItems;
}
