package com.dojangkok.backend.domain;

import com.dojangkok.backend.common.entity.BaseCreatedTimeEntity;
import com.dojangkok.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "lifestyle_item",
        indexes = {
                @Index(name = "idx_lifestyle_item_version_id", columnList = "lifestyle_version_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LifestyleItem extends BaseCreatedTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lifestyle_version_id")
    private LifestyleVersion lifestyleVersion;

    @Builder
    private LifestyleItem(String content, LifestyleVersion lifestyleVersion) {
        this.content = content;
        this.lifestyleVersion = lifestyleVersion;
    }

    public static LifestyleItem createLifestyleItem(String content, LifestyleVersion lifestyleVersion) {
        return LifestyleItem.builder()
                .content(content)
                .lifestyleVersion(lifestyleVersion)
                .build();
    }
}

