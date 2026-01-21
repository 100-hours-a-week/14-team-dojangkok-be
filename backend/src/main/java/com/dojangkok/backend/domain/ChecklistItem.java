package com.dojangkok.backend.domain;

import com.dojangkok.backend.common.entity.BaseTimeEntity;
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
        name = "checklist_item",
        indexes = {
                @Index(name = "idx_checklist_item_checklist_id", columnList = "checklist_id")
        }
)
public class ChecklistItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "checklist_id", nullable = false)
    private Checklist checklist;

    @Builder
    private ChecklistItem(String content, boolean isCompleted, Checklist checklist) {
        this.content = content;
        this.isCompleted = isCompleted;
        this.checklist = checklist;
    }

    public static ChecklistItem createChecklistItem(String content, Checklist checklist) {
        return ChecklistItem.builder()
                .content(content)
                .isCompleted(false)
                .checklist(checklist)
                .build();
    }

    public void complete() {
        this.isCompleted = true;
    }

    public void unComplete() {
        this.isCompleted = false;
    }

}

