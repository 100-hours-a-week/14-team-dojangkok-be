package com.dojangkok.backend.dto.easycontract;

import com.dojangkok.backend.domain.enums.EasyContractStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class EasyContractCreateResponseDto {

    @JsonProperty("easy_contract_id")
    private Long easyContractId;

    @JsonProperty("correlation_id")
    private String correlationId;

    private EasyContractStatus status;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
