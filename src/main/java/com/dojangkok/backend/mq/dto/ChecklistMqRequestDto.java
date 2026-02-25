package com.dojangkok.backend.mq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChecklistMqRequestDto {

    private String correlationId;
    private Long templateId;
    private Long memberId;
    private List<String> keywords;
}
