package com.dojangkok.backend.dto.checklist;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChecklistTemplateItemDto {

    @JsonProperty("checklist_template_item_id")
    private Long checklistTemplateItemId;

    private String content;
}
