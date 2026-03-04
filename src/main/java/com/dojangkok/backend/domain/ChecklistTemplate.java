package com.dojangkok.backend.domain;

import com.dojangkok.backend.common.entity.BaseCreatedTimeEntity;
import com.dojangkok.backend.domain.enums.ChecklistTemplateStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "checklist_template",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_checklist_template_lifestyle_version_id", columnNames = "lifestyle_version_id")
        }
)
public class ChecklistTemplate extends BaseCreatedTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ChecklistTemplateStatus checklistTemplateStatus;

    // OneToOne 대신 ManyToOne으로 열어두고 unique로 제약
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lifestyle_version_id", nullable = false)
    private LifestyleVersion lifestyleVersion;

    @Builder
    private ChecklistTemplate(LifestyleVersion lifestyleVersion, ChecklistTemplateStatus checklistTemplateStatus) {
        this.lifestyleVersion = lifestyleVersion;
        this.checklistTemplateStatus = checklistTemplateStatus;
    }

    public static ChecklistTemplate createChecklistTemplate(LifestyleVersion lifestyleVersion) {
        return ChecklistTemplate.builder()
                .lifestyleVersion(lifestyleVersion)
                .checklistTemplateStatus(ChecklistTemplateStatus.PROCESSING)
                .build();
    }

    public void updateStatus(ChecklistTemplateStatus status) {
        this.checklistTemplateStatus = status;
    }
}
