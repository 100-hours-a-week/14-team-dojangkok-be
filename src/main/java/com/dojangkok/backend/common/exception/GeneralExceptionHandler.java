package com.dojangkok.backend.common.exception;

import com.dojangkok.backend.common.dto.ErrorResponseDto;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GeneralExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorResponseDto> generalExceptionHandler(GeneralException e) {
        HttpStatusCode status = HttpStatusCode.valueOf(e.getCode().getStatus());
        ErrorResponseDto body = new ErrorResponseDto(e.getCode());
        return ResponseEntity
                .status(status)
                .body(body);

    }
}
