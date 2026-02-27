package com.dojangkok.backend.mq.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EasyContractMqResponseDto extends AiResponseDto {

    @JsonProperty("easy_contract_id")
    private Long easyContractId;

    private String content;
}
