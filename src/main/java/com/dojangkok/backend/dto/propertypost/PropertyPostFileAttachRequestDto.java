package com.dojangkok.backend.dto.propertypost;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PropertyPostFileAttachRequestDto {

    @NotEmpty(message = "첨부할 파일 목록은 비어있을 수 없습니다.")
    @Valid
    private List<PropertyPostFileAttachItemDto> items;
}
