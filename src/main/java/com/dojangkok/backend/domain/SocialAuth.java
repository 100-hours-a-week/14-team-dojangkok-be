package com.dojangkok.backend.domain;

import com.dojangkok.backend.common.entity.BaseCreatedTimeEntity;
import com.dojangkok.backend.domain.enums.Provider;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(
        name = "social_auth",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_social_auth_provider_provider_id",
                        columnNames = {"provider", "provider_id"}
                ),
                @UniqueConstraint(
                        name = "uk_social_auth_member",
                        columnNames = {"member_id"}
                )
        },
        indexes = {
                @Index(name = "idx_social_auth_provider", columnList = "provider")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialAuth extends BaseCreatedTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "social_auth_id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", length = 20, nullable = false)
    private Provider provider;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(name = "email")
    private String email;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attributes", columnDefinition = "json", nullable = false)
    private Map<String, Object> attributes;

    @Column(name = "last_logged_in_at", nullable = false)
    private LocalDateTime lastLoggedInAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Builder
    private SocialAuth(Member member, Provider provider, String providerId, String email,
                       Map<String, Object> attributes, LocalDateTime lastLoggedInAt
    ) {
        this.member = member;
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
        this.attributes = attributes == null ? new HashMap<>() : new HashMap<>(attributes);
        this.lastLoggedInAt = lastLoggedInAt;
    }

    public static SocialAuth createSocialAuth(Member member, Provider provider, String providerId, 
                                               String email, Map<String, Object> attributes) {
        return SocialAuth.builder()
                .member(member)
                .provider(provider)
                .providerId(providerId)
                .email(email)
                .attributes(attributes)
                .lastLoggedInAt(LocalDateTime.now())
                .build();
    }

    public void syncLastLoggedInAt() {
        this.lastLoggedInAt = LocalDateTime.now();
    }

    public void updateAttributes(Map<String, Object> attributes) {
        this.attributes = attributes == null ? new HashMap<>() : new HashMap<>(attributes);
        this.lastLoggedInAt = LocalDateTime.now();
    }
}
