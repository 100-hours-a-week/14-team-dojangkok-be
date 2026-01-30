package com.dojangkok.backend.dto.easycontract;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class EasyContractFileRequestDto {

    @NotEmpty(message = "파일 ID 목록은 비어있을 수 없습니다.")
    @Size(max = 5, message = "최대 5개까지 업로드할 수 있습니다.")
    @JsonProperty("file_asset_ids")
    private List<Long> fileAssetIds;

}
