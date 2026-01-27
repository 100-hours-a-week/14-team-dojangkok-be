package com.dojangkok.backend.dto.easycontract;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class EasyContractGenerateRequestDto {

    @JsonProperty("files")
    private List<EasyContractFileDto> files;
}