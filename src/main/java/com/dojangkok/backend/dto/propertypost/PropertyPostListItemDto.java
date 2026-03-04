package com.dojangkok.backend.dto.propertypost;

import com.dojangkok.backend.domain.enums.DealStatus;
import com.dojangkok.backend.domain.enums.PostStatus;
import com.dojangkok.backend.domain.enums.PropertyType;
import com.dojangkok.backend.domain.enums.RentType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class PropertyPostListItemDto {

    @JsonProperty("property_post_id")
    private Long propertyPostId;

    private String title;

    @JsonProperty("address_main")
    private String addressMain;

    @JsonProperty("price_main")
    private Long priceMain;

    @JsonProperty("price_monthly")
    private Integer priceMonthly;

    @JsonProperty("rent_type")
    private RentType rentType;

    @JsonProperty("property_type")
    private PropertyType propertyType;

    @JsonProperty("exclusive_area_m2")
    private BigDecimal exclusiveAreaM2;

    private BigDecimal floor;

    @JsonProperty("maintenance_fee")
    private Integer maintenanceFee;

    @JsonProperty("deal_status")
    private DealStatus dealStatus;

    @JsonProperty("post_status")
    private PostStatus postStatus;

    @JsonProperty("is_verified")
    private boolean verified;

    @JsonProperty("is_hidden")
    private boolean hidden;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("is_bookmarked")
    private boolean bookmarked;

    private PropertyPostThumbnailDto thumbnail;
}
