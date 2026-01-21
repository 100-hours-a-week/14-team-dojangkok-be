package com.dojangkok.backend.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "withdrawn_member",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_withdrawn_member_member_id", columnNames = {"member_id"})
        },
        indexes = {
                @Index(name = "idx_withdrawn_member_deleted_at", columnList = "deleted_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WithdrawnMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "withdrawn_member_id", nullable = false)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "nickname", length = 30, nullable = false)
    private String nickname;

    @Column(name = "deleted_at", nullable = false)
    private LocalDateTime deletedAt;

    @Builder
    private WithdrawnMember(Long memberId, String email, String nickname, LocalDateTime deletedAt) {
        this.memberId = memberId;
        this.email = email;
        this.nickname = nickname;
        this.deletedAt = deletedAt;
    }

    public static WithdrawnMember createWithdrawnMember(Member member) {
        return WithdrawnMember.builder()
                .memberId(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .deletedAt(LocalDateTime.now())
                .build();
    }
}

