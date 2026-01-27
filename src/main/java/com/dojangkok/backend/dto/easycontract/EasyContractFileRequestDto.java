package com.dojangkok.backend.dto.easycontract;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class EasyContractFileRequestDto {

    @NotEmpty(message = "파일 ID 목록은 비어있을 수 없습니다.")
    @JsonProperty("file_asset_ids")
    private List<Long> fileAssetIds;

}
