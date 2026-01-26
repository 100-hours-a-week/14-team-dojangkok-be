package com.dojangkok.backend.dto.easycontract;

import com.dojangkok.backend.domain.enums.EasyContractStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class EasyContractRetryResponseDto {

    @JsonProperty("easy_contract_id")
    private Long easyContractId;

    private EasyContractStatus status;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
