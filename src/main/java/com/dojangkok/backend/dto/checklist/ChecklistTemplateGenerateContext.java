package com.dojangkok.backend.dto.checklist;

import lombok.Builder;

@Builder
public record ChecklistTemplateGenerateContext(
        Long templateId,
        ChecklistGenerateRequestDto checklistGenerateRequestDto
) {}

