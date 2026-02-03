package com.dojangkok.backend.domain;

import com.dojangkok.backend.common.entity.BaseTimeEntity;
import com.dojangkok.backend.domain.enums.MemberStatus;
import com.dojangkok.backend.domain.enums.OnboardingStatus;
import com.dojangkok.backend.domain.enums.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "member",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_member_email",
                        columnNames = {"email"}
                ),
                @UniqueConstraint(
                        name = "uk_member_nickname",
                        columnNames = {"nickname"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "nickname", length = 30)
    private String nickname;

    @Column(name = "email", length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_status", nullable = false, length = 20)
    private MemberStatus memberStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "onboarding_status", nullable = false, length = 20)
    private OnboardingStatus onboardingStatus;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "last_logged_in_at", nullable = false)
    private LocalDateTime lastLoggedInAt;

    @Builder
    private Member(String nickname, String email, Role role, MemberStatus memberStatus, OnboardingStatus onboardingStatus, String profileImage, LocalDateTime lastLoggedInAt) {
        this.nickname = nickname;
        this.email = email;
        this.role = role;
        this.memberStatus = memberStatus;
        this.profileImage = profileImage;
        this.onboardingStatus = onboardingStatus;
        this.lastLoggedInAt = lastLoggedInAt;
    }

    public static Member createMember(String nickname, String email, Role role, String profileImage
    ) {
        return Member.builder()
                .nickname(nickname)
                .email(email)
                .role(role)
                .memberStatus(MemberStatus.ACTIVE)
                .onboardingStatus(OnboardingStatus.NICKNAME)
                .profileImage(profileImage)
                .lastLoggedInAt(LocalDateTime.now())
                .build();
    }

    public void updateOnboardingStatus(OnboardingStatus onboardingStatus) {
        this.onboardingStatus = onboardingStatus;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateLastLoggedInAt() {
        this.lastLoggedInAt = LocalDateTime.now();
    }
}
