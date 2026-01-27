package com.dojangkok.backend.dto.checklist;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChecklistTemplateResponseDto {

    @JsonProperty("checklist_template_id")
    private Long checklistTemplateId;

    @JsonProperty("checklist_template_items")
    private List<ChecklistTemplateItemDto> checklistTemplateItems;
}
