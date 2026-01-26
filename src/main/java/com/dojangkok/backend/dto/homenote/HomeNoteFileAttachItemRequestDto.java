package com.dojangkok.backend.dto.homenote;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HomeNoteFileAttachItemRequestDto {

    @NotNull(message = "파일 ID는 필수입니다.")
    @JsonProperty("file_asset_id")
    private Long fileAssetId;
}
