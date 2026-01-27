package com.dojangkok.backend.common.dto;

import com.dojangkok.backend.common.enums.Code;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

@Getter
@JsonPropertyOrder({"code", "message", "data"})
public class DataResponseDto<T> extends ResponseDto {

    private final T data;

    public DataResponseDto(Code code, T data) {
        super(code, code.getMessage());
        this.data = data;
    }

    public DataResponseDto(Code code, String message, T data) {
        super(code, message);
        this.data = data;
    }
}
