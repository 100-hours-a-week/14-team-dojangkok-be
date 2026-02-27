package com.dojangkok.backend.dto.propertypost;

import com.dojangkok.backend.domain.enums.PropertyType;
import com.dojangkok.backend.domain.enums.RentType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class PropertyPostCreateRequestDto {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @JsonProperty("easy_contract_id")
    private Long easyContractId;

    @NotBlank(message = "주소는 필수입니다.")
    @JsonProperty("address_main")
    private String addressMain;

    @JsonProperty("address_detail")
    private String addressDetail;

    @NotNull(message = "가격은 필수입니다.")
    @JsonProperty("price_main")
    private Long priceMain;

    @JsonProperty("price_monthly")
    private Integer priceMonthly;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    @NotNull(message = "매물 유형은 필수입니다.")
    @JsonProperty("property_type")
    private PropertyType propertyType;

    @NotNull(message = "임대 유형은 필수입니다.")
    @JsonProperty("rent_type")
    private RentType rentType;

    @NotNull(message = "전용 면적은 필수입니다.")
    @JsonProperty("exclusive_area_m2")
    private BigDecimal exclusiveAreaM2;

    @NotNull(message = "지하 여부는 필수입니다.")
    @JsonProperty("is_basement")
    private Boolean isBasement;

    @NotNull(message = "층수는 필수입니다.")
    private BigDecimal floor;

    @NotNull(message = "관리비는 필수입니다.")
    @JsonProperty("maintenance_fee")
    private Integer maintenanceFee;
}
