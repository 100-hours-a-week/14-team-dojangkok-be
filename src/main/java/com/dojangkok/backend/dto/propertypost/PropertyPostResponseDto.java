package com.dojangkok.backend.dto.propertypost;

import com.dojangkok.backend.domain.enums.DealStatus;
import com.dojangkok.backend.domain.enums.PostStatus;
import com.dojangkok.backend.domain.enums.PropertyType;
import com.dojangkok.backend.domain.enums.RentType;
import com.dojangkok.backend.dto.member.PostWriterInfoDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PropertyPostResponseDto {

    private PostWriterInfoDto writer;

    @JsonProperty("property_post_id")
    private Long propertyPostId;

    private String title;

    private String address;

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
    private boolean basement;

    private BigDecimal floor;

    @JsonProperty("maintenance_fee")
    private Integer maintenanceFee;

    @JsonProperty("post_status")
    private PostStatus postStatus;

    @JsonProperty("deal_status")
    private DealStatus dealStatus;

    @JsonProperty("is_hidden")
    private boolean hidden;

    @JsonProperty("is_verified")
    private Boolean verified;

    @JsonProperty("is_bookmarked")
    private boolean bookmarked;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    private List<PropertyPostImageDto> images;
}
