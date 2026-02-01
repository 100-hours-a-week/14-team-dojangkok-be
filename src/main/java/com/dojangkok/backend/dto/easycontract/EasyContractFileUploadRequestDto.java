package com.dojangkok.backend.dto.easycontract;

import com.dojangkok.backend.dto.fileasset.PresignedUrlItemRequestDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class EasyContractFileUploadRequestDto {

    @Valid
    @NotEmpty(message = "업로드할 파일 정보가 필요합니다.")
    @Size(max = 5, message = "한 번에 최대 5개의 파일만 업로드할 수 있습니다.")
    @JsonProperty("file_items")
    private List<PresignedUrlItemRequestDto> fileItems;
}
