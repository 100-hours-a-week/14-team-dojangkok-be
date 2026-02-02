package com.dojangkok.backend.dto.easycontract;

import com.dojangkok.backend.dto.fileasset.PresignedUrlItemResponseDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class EasyContractFileUploadResponseDto {

    @JsonProperty("file_items")
    private List<PresignedUrlItemResponseDto> fileItems;
}
