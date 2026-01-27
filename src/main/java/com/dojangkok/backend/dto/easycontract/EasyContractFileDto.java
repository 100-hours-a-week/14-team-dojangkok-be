package com.dojangkok.backend.dto.easycontract;

import com.dojangkok.backend.domain.enums.FileType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EasyContractFileDto {

    @JsonProperty("url")
    private String url;

    @JsonProperty("filename")
    private String fileName;

    @JsonProperty("doc_type")
    private FileType fileType;
}
