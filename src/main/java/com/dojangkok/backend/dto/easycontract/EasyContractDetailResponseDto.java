package com.dojangkok.backend.dto.easycontract;

import com.dojangkok.backend.domain.enums.EasyContractStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class EasyContractDetailResponseDto {

    @JsonProperty("easy_contract_id")
    private Long easyContractId;

    private String title;

    private String content;

    private EasyContractStatus status;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
