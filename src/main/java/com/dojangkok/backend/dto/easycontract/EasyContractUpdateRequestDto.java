package com.dojangkok.backend.dto.easycontract;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EasyContractUpdateRequestDto {

    @NotBlank(message = "제목은 비어있을 수 없습니다.")
    private String title;
}
