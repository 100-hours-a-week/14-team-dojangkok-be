package com.dojangkok.backend.dto.easycontract;

import com.dojangkok.backend.domain.enums.DocType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EasyContractFileDto {

    @JsonProperty("doc_type")
    private DocType docType;

    @JsonProperty("filename")
    private String fileName;

    @JsonProperty("url")
    private String url;
}
