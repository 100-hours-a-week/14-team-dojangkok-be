package com.dojangkok.backend.domain;

import com.dojangkok.backend.common.entity.BaseTimeEntity;
import com.dojangkok.backend.domain.enums.EasyContractStatus;
import com.dojangkok.backend.domain.enums.FileAssetStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "easy_contract",
        indexes = {
                @Index(name = "idx_easy_contract_member_created_at_id", columnList = "member_id, created_at, id"),
                @Index(name = "idx_easy_contract_status_created_at", columnList = "status, created_at")
        }
)
public class EasyContract extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EasyContractStatus status;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Builder(access = AccessLevel.PRIVATE)
    private EasyContract(Member member, String content, EasyContractStatus status, LocalDateTime deletedAt) {
        this.member = member;
        this.content = content;
        this.status = status;
        this.deletedAt = deletedAt;
    }

    public static EasyContract createEasyContract(Member member, String content) {
        return EasyContract.builder()
                .member(member)
                .content(content)
                .status(EasyContractStatus.PROCESSING)
                .build();
    }

    public void markCompleted() {
        this.status = EasyContractStatus.COMPLETED;
    }
}
