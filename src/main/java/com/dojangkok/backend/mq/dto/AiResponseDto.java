package com.dojangkok.backend.mq.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = EasyContractMqResponseDto.class, name = "easy-contract"),
        @JsonSubTypes.Type(value = ChecklistMqResponseDto.class, name = "checklist")
})
    public abstract class AiResponseDto {

    private String type;
    private String correlationId;
    private Long memberId;
    private boolean success;
    private String errorMessage;
}
