package com.dojangkok.backend.dto.fileasset;

import com.dojangkok.backend.domain.enums.FileType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PresignedUrlItemRequestDto {

    @NotNull(message = "파일 타입은 필수입니다.")
    @JsonProperty("file_type")
    private FileType fileType;

    @NotBlank(message = "컨텐츠 타입은 필수입니다.")
    @JsonProperty("content_type")
    private String contentType;

    @NotBlank(message = "파일명은 필수입니다.")
    @JsonProperty("file_name")
    private String fileName;
}
