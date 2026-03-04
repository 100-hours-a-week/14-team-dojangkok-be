package com.dojangkok.backend.dto.propertypost;

import com.dojangkok.backend.domain.enums.DealStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PropertyPostDealStatusUpdateRequestDto {

    @NotNull(message = "deal_status 값은 필수입니다.")
    @JsonProperty("deal_status")
    private DealStatus dealStatus;
}
