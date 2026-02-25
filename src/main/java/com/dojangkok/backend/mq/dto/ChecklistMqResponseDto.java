package com.dojangkok.backend.mq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChecklistMqResponseDto extends AiResponseDto {

    private Long templateId;
    private List<String> checklists;
}
