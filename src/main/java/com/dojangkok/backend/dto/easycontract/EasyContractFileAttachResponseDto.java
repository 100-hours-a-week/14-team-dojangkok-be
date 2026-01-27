package com.dojangkok.backend.dto.easycontract;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class EasyContractFileAttachResponseDto {

    private List<EasyContractFileAttachItemDto> fileItems;
}
