package com.dojangkok.backend.mq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EasyContractMqResponseDto extends AiResponseDto {

    private Long easyContractId;
    private String content;
}
