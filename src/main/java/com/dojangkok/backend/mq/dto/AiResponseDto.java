package com.dojangkok.backend.mq.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = EasyContractMqResponseDto.class, name = "easy-contract"),
        @JsonSubTypes.Type(value = ChecklistMqResponseDto.class, name = "checklist")
})
public abstract class AiResponseDto {

    private String type;

    @JsonProperty("correlation_id")
    private String correlationId;

    @JsonProperty("member_id")
    private Long memberId;

    private boolean success;

    @JsonProperty("error_message")
    private String errorMessage;
}
