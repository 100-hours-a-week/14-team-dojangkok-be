package com.dojangkok.backend.mapper;

import com.dojangkok.backend.domain.Checklist;
import com.dojangkok.backend.domain.ChecklistItem;
import com.dojangkok.backend.domain.ChecklistTemplate;
import com.dojangkok.backend.domain.ChecklistTemplateItem;
import com.dojangkok.backend.dto.checklist.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChecklistMapper {

    public ChecklistTemplateResponseDto toChecklistTemplateResponseDto(ChecklistTemplate template,
                                                                        List<ChecklistTemplateItem> items) {
        List<ChecklistTemplateItemDto> itemDtos = items.stream()
                .map(this::toChecklistTemplateItemDto)
                .toList();

        return ChecklistTemplateResponseDto.builder()
                .checklistTemplateId(template.getId())
                .checklistTemplateItems(itemDtos)
                .build();
    }

    public ChecklistTemplateItemDto toChecklistTemplateItemDto(ChecklistTemplateItem item) {
        return ChecklistTemplateItemDto.builder()
                .checklistTemplateItemId(item.getId())
                .content(item.getContent())
                .build();
    }

    public ChecklistResponseDto toChecklistResponseDto(Checklist checklist, List<ChecklistItem> items) {
        List<ChecklistItemDto> itemDtoList = items.stream()
                .map(this::toChecklistItemDto)
                .toList();

        return ChecklistResponseDto.builder()
                .checklistId(checklist.getId())
                .checklistItems(itemDtoList)
                .build();
    }

    public ChecklistItemDto toChecklistItemDto(ChecklistItem item) {
        return ChecklistItemDto.builder()
                .checklistItemId(item.getId())
                .content(item.getContent())
                .completed(item.isCompleted())
                .build();
    }


    public ChecklistUpdateResponseDto toChecklistSaveResponseDto(Checklist checklist, List<ChecklistItem> items) {
        List<ChecklistItemDto> itemDtos = items.stream()
                .map(this::toChecklistItemDto)
                .toList();

        return ChecklistUpdateResponseDto.builder()
                .checklistId(checklist.getId())
                .checklists(itemDtos)
                .build();
    }


    public ChecklistItemStatusResponseDto toChecklistItemStatusResponseDto(ChecklistItem item) {
        return ChecklistItemStatusResponseDto.builder()
                .checklistItemId(item.getId())
                .completed(item.isCompleted())
                .build();
    }
}
