package com.dojangkok.backend.domain;

import com.dojangkok.backend.common.entity.BaseTimeEntity;
import com.dojangkok.backend.domain.enums.EasyContractStatus;
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

    @Column(name = "title", length = 100)
    private String title;

    @Lob
    @Column(name = "content", columnDefinition = "TEXT")
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
    private EasyContract(Member member, String title, String content, EasyContractStatus status, LocalDateTime deletedAt) {
        this.member = member;
        this.title = title;
        this.content = content;
        this.status = status;
        this.deletedAt = deletedAt;
    }

    public static EasyContract createEasyContract(Member member, String title, String content) {
        return EasyContract.builder()
                .member(member)
                .title(title)
                .content(content)
                .status(EasyContractStatus.COMPLETED)
                .deletedAt(null)
                .build();
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateContent(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void markCompleted(String content) {
        this.content = content;
        this.status = EasyContractStatus.COMPLETED;
    }

    public void markFailed() {
        this.status = EasyContractStatus.FAILED;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean canRetry() {
        return this.status == EasyContractStatus.FAILED || this.status == EasyContractStatus.PROCESSING;
    }
}
