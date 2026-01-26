package com.dojangkok.backend.dto.easycontract;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class EasyContractUpdateResponseDto {

    @JsonProperty("easy_contract_id")
    private Long easyContractId;

    private String title;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
