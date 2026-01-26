package com.dojangkok.backend.dto.easycontract;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class EasyContractAssetListResponseDto {

    private List<EasyContractAssetItemDto> fileAssetList;
}
