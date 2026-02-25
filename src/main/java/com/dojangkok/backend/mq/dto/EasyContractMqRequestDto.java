package com.dojangkok.backend.mq.dto;

import com.dojangkok.backend.dto.easycontract.EasyContractFileDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EasyContractMqRequestDto {

    @JsonProperty("correlation_id")
    private String correlationId;

    @JsonProperty("easy_contract_id")
    private Long easyContractId;

    private List<EasyContractFileDto> files;
}
