package com.dojangkok.backend.domain;

import com.dojangkok.backend.common.entity.BaseCreatedTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "checklist_template_item",
        indexes = {
                @Index(name = "idx_checklist_template_item_template_id", columnList = "checklist_template_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChecklistTemplateItem extends BaseCreatedTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "checklist_template_id", nullable = false)
    private ChecklistTemplate checklistTemplate;

    @Builder
    private ChecklistTemplateItem(String content, ChecklistTemplate checklistTemplate) {
        this.content = content;
        this.checklistTemplate = checklistTemplate;
    }

    public static ChecklistTemplateItem createChecklistTemplateItem(String content, ChecklistTemplate checklistTemplate) {
        return ChecklistTemplateItem.builder()
                .content(content)
                .checklistTemplate(checklistTemplate)
                .build();
    }
}

