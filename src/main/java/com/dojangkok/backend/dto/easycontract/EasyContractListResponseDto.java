package com.dojangkok.backend.dto.easycontract;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class EasyContractListResponseDto {

    private int limit;

    @JsonProperty("has_next")
    private boolean hasNext;

    @JsonProperty("next_cursor")
    private String nextCursor;

    private List<EasyContractListItemDto> easyContractListItemList;
}
