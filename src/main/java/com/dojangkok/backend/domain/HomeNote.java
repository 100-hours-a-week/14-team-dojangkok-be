package com.dojangkok.backend.domain;

import com.dojangkok.backend.common.entity.BaseCreatedTimeEntity;
import com.dojangkok.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "home_note",
        indexes = {
                @Index(name = "idx_home_note_member_id_created_at", columnList = "member_id, created_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HomeNote extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    private HomeNote(Member member, String title, LocalDateTime deletedAt) {
        this.member = member;
        this.title = title;
        this.deletedAt = deletedAt;
    }

    public static HomeNote createHomeNote(Member member, String title) {
        return HomeNote.builder()
                .member(member)
                .title(title)
                .deletedAt(null)
                .build();
    }

    public void changeTitle(String title) {
        this.title = title;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}

