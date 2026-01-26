package com.dojangkok.backend.dto.fileasset;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PresignedUrlRequestDto {

    @Valid
    @NotEmpty(message = "업로드할 파일 정보가 필요합니다.")
    @JsonProperty("file_items")
    private List<PresignedUrlItemRequestDto> fileItems;
}
