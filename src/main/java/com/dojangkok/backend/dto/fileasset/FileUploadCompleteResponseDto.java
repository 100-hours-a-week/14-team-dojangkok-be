package com.dojangkok.backend.dto.fileasset;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FileUploadCompleteResponseDto {

    @Valid
    @NotBlank
    @JsonProperty("file_items")
    private List<FileUploadCompleteItemResponseDto> fileItems;
}
