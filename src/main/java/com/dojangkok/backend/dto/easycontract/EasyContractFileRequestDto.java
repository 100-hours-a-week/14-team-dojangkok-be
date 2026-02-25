package com.dojangkok.backend.dto.easycontract;

import com.dojangkok.backend.domain.enums.DocType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class EasyContractFileRequestDto {

    @NotEmpty(message = "파일 그룹은 비어있을 수 없습니다.")
    @Valid
    private List<FileGroup> files;

    @Getter
    @NoArgsConstructor
    public static class FileGroup {

        @NotNull(message = "문서 유형은 필수입니다.")
        @JsonProperty("doc_type")
        private DocType docType;

        @NotEmpty(message = "파일 ID 목록은 비어있을 수 없습니다.")
        @Size(max = 5, message = "문서 유형당 최대 5개까지 업로드할 수 있습니다.")
        @JsonProperty("file_asset_ids")
        private List<Long> fileAssetIds;
    }
}
