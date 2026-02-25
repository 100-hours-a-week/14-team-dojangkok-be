package com.dojangkok.backend.mq.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EasyContractCancelRequestDto {

    @JsonProperty("easy_contract_id")
    private Long easyContractId;
}
