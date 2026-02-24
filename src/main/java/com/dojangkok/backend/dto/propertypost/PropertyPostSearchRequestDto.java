package com.dojangkok.backend.dto.propertypost;

import com.dojangkok.backend.domain.enums.PropertyType;
import com.dojangkok.backend.domain.enums.RentType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class PropertyPostSearchRequestDto {

    private String keyword;

    private List<PropertyType> propertyTypes;

    private List<RentType> rentTypes;

    private Long priceMainMin;

    private Long priceMainMax;

    private Integer priceMonthlyMin;

    private Integer priceMonthlyMax;

    private BigDecimal areaMin;

    private BigDecimal areaMax;

    private Boolean isVerified;

    private String cursor;
}
