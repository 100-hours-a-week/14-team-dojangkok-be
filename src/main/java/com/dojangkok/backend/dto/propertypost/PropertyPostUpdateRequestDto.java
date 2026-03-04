package com.dojangkok.backend.dto.propertypost;

import com.dojangkok.backend.domain.enums.PropertyType;
import com.dojangkok.backend.domain.enums.RentType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class PropertyPostUpdateRequestDto {

    private String title;

    @JsonProperty("easy_contract_id")
    private Long easyContractId;

    @JsonProperty("address_main")
    private String addressMain;

    @JsonProperty("address_detail")
    private String addressDetail;

    @JsonProperty("price_main")
    private Long priceMain;

    @JsonProperty("price_monthly")
    private Integer priceMonthly;

    private String content;

    @JsonProperty("property_type")
    private PropertyType propertyType;

    @JsonProperty("rent_type")
    private RentType rentType;

    @JsonProperty("exclusive_area_m2")
    private BigDecimal exclusiveAreaM2;

    @JsonProperty("is_basement")
    private Boolean isBasement;

    private BigDecimal floor;

    @JsonProperty("maintenance_fee")
    private Integer maintenanceFee;
}
