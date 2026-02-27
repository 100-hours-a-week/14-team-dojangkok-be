package com.dojangkok.backend.dto.propertypost;

import com.dojangkok.backend.dto.fileasset.FileUploadCompleteItemResponseDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PropertyPostFileCompleteResponseDto {

    @JsonProperty("success_items")
    private List<FileUploadCompleteItemResponseDto> successItems;

    @JsonProperty("failed_items")
    private List<PropertyPostFileCompleteFailedItemDto> failedItems;
}
