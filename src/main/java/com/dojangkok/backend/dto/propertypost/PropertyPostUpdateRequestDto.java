package com.dojangkok.backend.dto.propertypost;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PropertyPostUpdateRequestDto {

    private String title;

    @JsonProperty("easy_contract_id")
    private Long easyContractId;

    @JsonProperty("price_main")
    private Long priceMain;

    @JsonProperty("price_monthly")
    private Integer priceMonthly;

    private String content;
}
