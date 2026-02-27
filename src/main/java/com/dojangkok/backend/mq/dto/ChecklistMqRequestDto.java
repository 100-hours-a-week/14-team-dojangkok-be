package com.dojangkok.backend.mq.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("correlation_id")
    private String correlationId;

    @JsonProperty("template_id")
    private Long templateId;

    @JsonProperty("member_id")
    private Long memberId;

    private List<String> keywords;
}
