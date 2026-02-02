package com.dojangkok.backend.dto.easycontract;

import com.dojangkok.backend.dto.fileasset.FileUploadCompleteItemResponseDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class EasyContractFileCompleteResponseDto {

    @JsonProperty("file_items")
    private List<FileUploadCompleteItemResponseDto> fileItems;
}
