package com.dojangkok.backend.dto.propertypost;

import com.dojangkok.backend.domain.enums.DealStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PropertyPostDealStatusUpdateResponseDto {

    @JsonProperty("property_post_id")
    private Long propertyPostId;

    @JsonProperty("deal_status")
    private DealStatus dealStatus;
}
