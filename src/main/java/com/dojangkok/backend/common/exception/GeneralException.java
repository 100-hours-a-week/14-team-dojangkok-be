package com.dojangkok.backend.common.exception;

import com.dojangkok.backend.common.enums.Code;
import lombok.Getter;

@Getter
public class GeneralException extends RuntimeException {

    private final Code code;

    public GeneralException(Code code) {
        super(code.getMessage());
        this.code = code;
    }
}
