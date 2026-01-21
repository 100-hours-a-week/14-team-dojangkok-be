package com.dojangkok.backend.domain;

import com.dojangkok.backend.common.entity.BaseTimeEntity;
import com.dojangkok.backend.domain.enums.DealStatus;
import com.dojangkok.backend.domain.enums.PostStatus;
import com.dojangkok.backend.domain.enums.PropertyType;
import com.dojangkok.backend.domain.enums.RentType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "property_post",
        indexes = {
                @Index(
                        name = "idx_property_post_hidden_post_deal_created",
                        columnList = "is_hidden, post_status, deal_status, created_at, id"
                ),
                @Index(
                        name = "idx_property_post_member_hidden_deal_created",
                        columnList = "member_id, is_hidden, deal_status, created_at, id"
                )
        }
)
public class PropertyPost extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "easy_contract_id")
    private EasyContract easyContract;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "address_main", nullable = false)
    private String addressMain;

    @Column(name = "address_detail", nullable = false, length = 100)
    private String addressDetail;

    @Column(name = "price_main", nullable = false)
    private Long priceMain;

    @Column(name = "price_monthly")
    private Integer priceMonthly;

    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "property_type", nullable = false, length = 20)
    private PropertyType propertyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "rent_type", nullable = false, length = 20)
    private RentType rentType;

    @Column(name = "exclusive_area_m2", nullable = false, precision = 6, scale = 2)
    private BigDecimal exclusiveAreaM2;

    @Column(name = "is_basement", nullable = false)
    private boolean isBasement;

    @Column(name = "floor", nullable = false, precision = 4, scale = 1)
    private BigDecimal floor;

    @Column(name = "maintenance_fee", nullable = false)
    private Integer maintenanceFee;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_status", nullable = false, length = 20)
    private PostStatus postStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "deal_status", nullable = false, length = 20)
    private DealStatus dealStatus;

    @Column(name = "is_hidden", nullable = false)
    private boolean isHidden;

    @Column(name = "is_verified")
    private Boolean isVerified;

    @Lob
    @Column(name = "search_text", nullable = false, columnDefinition = "TEXT")
    private String searchText;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "propertyPost", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PropertyPostFile> propertyPostFiles = new ArrayList<>();

    @Builder
    private PropertyPost(Member member, EasyContract easyContract, String title, String addressMain, String addressDetail,
                         Long priceMain, Integer priceMonthly, String content, PropertyType propertyType, RentType rentType,
                         BigDecimal exclusiveAreaM2, boolean isBasement, BigDecimal floor, Integer maintenanceFee, PostStatus postStatus,
                         DealStatus dealStatus, boolean isHidden, Boolean isVerified, String searchText, LocalDateTime deletedAt) {

        this.member = member;
        this.easyContract = easyContract;
        this.title = title;
        this.addressMain = addressMain;
        this.addressDetail = addressDetail;
        this.priceMain = priceMain;
        this.priceMonthly = priceMonthly;
        this.content = content;
        this.propertyType = propertyType;
        this.rentType = rentType;
        this.exclusiveAreaM2 = exclusiveAreaM2;
        this.isBasement = isBasement;
        this.floor = floor;
        this.maintenanceFee = maintenanceFee;

        this.postStatus = postStatus;
        this.dealStatus = dealStatus;
        this.isHidden = isHidden;
        this.isVerified = isVerified;

        this.searchText = searchText;
        this.deletedAt = deletedAt;
    }

    public static PropertyPost createPropertyPost(Member member, EasyContract easyContract, String title, String addressMain, String addressDetail,
                                                  Long priceMain, Integer priceMonthly, String content, PropertyType propertyType, RentType rentType,
                                                  BigDecimal exclusiveAreaM2, boolean isBasement, BigDecimal floor, Integer maintenanceFee, boolean isVerified) {

        // search_text 정책: title + address_main + address_detail 결합
        String searchText = String.join(" ", title, addressMain);

        return PropertyPost.builder()
                .member(member)
                .easyContract(easyContract)
                .title(title)
                .addressMain(addressMain)
                .addressDetail(addressDetail)
                .priceMain(priceMain)
                .priceMonthly(priceMonthly)
                .content(content)
                .propertyType(propertyType)
                .rentType(rentType)
                .exclusiveAreaM2(exclusiveAreaM2)
                .isBasement(isBasement)
                .floor(floor)
                .maintenanceFee(maintenanceFee)
                .postStatus(PostStatus.ACTIVE)
                .dealStatus(DealStatus.TRADING)
                .isHidden(false)
                .isVerified(isVerified)
                .searchText(searchText)
                .build();
    }

    public void changeDealStatus(DealStatus dealStatus) {
        this.dealStatus = dealStatus;
        this.updatedAt = LocalDateTime.now();
    }

    public void changeHidden(boolean hidden) {
        this.isHidden = hidden;
        this.updatedAt = LocalDateTime.now();
    }

    public void archive() {
        this.postStatus = PostStatus.ARCHIVED;
        this.updatedAt = LocalDateTime.now();
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}
