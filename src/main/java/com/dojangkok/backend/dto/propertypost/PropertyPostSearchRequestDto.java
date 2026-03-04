package com.dojangkok.backend.dto.propertypost;

import com.dojangkok.backend.domain.enums.PropertyPostSort;
import com.dojangkok.backend.domain.enums.PropertyType;
import com.dojangkok.backend.domain.enums.RentType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PropertyPostSearchRequestDto {

    private String keyword;

    @JsonProperty("property_type")
    private List<PropertyType> propertyType;

    @JsonProperty("rent_type")
    private List<RentType> rentType;

    @JsonProperty("deposit_min")
    private Long depositMin;

    @JsonProperty("deposit_max")
    private Long depositMax;

    @JsonProperty("sale_price_min")
    private Long salePriceMin;

    @JsonProperty("sale_price_max")
    private Long salePriceMax;

    @JsonProperty("price_monthly_min")
    private Integer priceMonthlyMin;

    @JsonProperty("price_monthly_max")
    private Integer priceMonthlyMax;

    private PropertyPostSort sort;

    @JsonProperty("is_verified")
    private Boolean isVerified;
}
