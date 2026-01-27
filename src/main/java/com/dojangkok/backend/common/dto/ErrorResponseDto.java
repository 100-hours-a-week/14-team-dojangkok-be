package com.dojangkok.backend.common.dto;

import com.dojangkok.backend.common.enums.Code;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({"code", "message", "data"})
public class ErrorResponseDto extends ResponseDto {

    private final Object data = null;

    public ErrorResponseDto(Code code) {
        super(code, code.getMessage());
    }
}
