package com.dojangkok.backend.mq.dto;

import com.dojangkok.backend.dto.easycontract.EasyContractGenerateRequestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EasyContractMqRequestDto {

    private String correlationId;
    private Long easyContractId;
    private EasyContractGenerateRequestDto easyContractGenerateRequestDto;
}
