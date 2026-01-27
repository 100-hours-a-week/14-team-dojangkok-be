package com.dojangkok.backend.domain;

import com.dojangkok.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "lifestyle",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_lifestyle_member_id", columnNames = "member_id"),
                @UniqueConstraint(name = "uk_lifestyle_current_version_id", columnNames = "current_version_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Lifestyle extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_version_id")
    private LifestyleVersion currentVersion;

    @Builder
    private Lifestyle(Member member, LifestyleVersion currentVersion) {
        this.member = member;
        this.currentVersion = currentVersion;
    }

    public static Lifestyle createLifestyle(Member member, LifestyleVersion lifestyleVersion) {
        return Lifestyle.builder()
                .member(member)
                .currentVersion(lifestyleVersion)
                .build();
    }

    public void updateCurrentVersion(LifestyleVersion lifestyleVersion) {
        this.currentVersion = lifestyleVersion;
    }
}

