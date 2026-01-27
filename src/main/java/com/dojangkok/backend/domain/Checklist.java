package com.dojangkok.backend.domain;

import com.dojangkok.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "checklist",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_checklist_home_note_id", columnNames = "home_note_id")
        },
        indexes = {
                @Index(name = "idx_checklist_member_id", columnList = "member_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Checklist extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "home_note_id", nullable = false)
    private HomeNote homeNote;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "checklist_template_id", nullable = false)
    private ChecklistTemplate checklistTemplate;

    @Builder
    private Checklist(Member member, HomeNote homeNote, ChecklistTemplate checklistTemplate) {
        this.member = member;
        this.homeNote = homeNote;
        this.checklistTemplate = checklistTemplate;
    }

    public static Checklist createChecklist(Member member, HomeNote homeNote, ChecklistTemplate checklistTemplate) {
        return Checklist.builder()
                .member(member)
                .homeNote(homeNote)
                .checklistTemplate(checklistTemplate)
                .build();
    }
}

