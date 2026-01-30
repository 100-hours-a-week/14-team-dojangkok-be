package com.dojangkok.backend.controller;

import com.dojangkok.backend.auth.jwt.CurrentMemberId;
import com.dojangkok.backend.common.dto.DataResponseDto;
import com.dojangkok.backend.common.enums.Code;
import com.dojangkok.backend.dto.easycontract.*;
import com.dojangkok.backend.service.EasyContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/easy-contracts")
public class EasyContractController {

    private final EasyContractService easyContractService;

    @PostMapping
    public DataResponseDto<EasyContractCreateResponseDto> createEasyContract(@CurrentMemberId Long memberId,
                                                                             @Valid @RequestBody EasyContractFileRequestDto requestDto) {
        EasyContractCreateResponseDto responseDto = easyContractService.createEasyContract(memberId, requestDto);
        return new DataResponseDto<>(Code.CREATED_SUCCESS, "쉬운 계약서 생성 요청에 성공하였습니다.", responseDto);
    }

    @GetMapping
    public DataResponseDto<EasyContractListResponseDto> getEasyContractList(@CurrentMemberId Long memberId,
                                                                            @RequestParam(required = false) String cursor) {
        EasyContractListResponseDto responseDto = easyContractService.getEasyContractList(memberId, cursor);
        return new DataResponseDto<>(Code.SUCCESS, "쉬운 계약서 목록 조회에 성공하였습니다.", responseDto);
    }

    @GetMapping("/{easyContractId}")
    public DataResponseDto<EasyContractDetailResponseDto> getEasyContractDetail(@CurrentMemberId Long memberId, @PathVariable Long easyContractId) {
        EasyContractDetailResponseDto responseDto = easyContractService.getEasyContractDetail(memberId, easyContractId);
        return new DataResponseDto<>(Code.SUCCESS, "쉬운 계약서 상세 조회에 성공하였습니다.", responseDto);
    }

    @PostMapping("/{easyContractId}/files")
    public DataResponseDto<EasyContractFileAttachResponseDto> attachFiles(@CurrentMemberId Long memberId, @PathVariable Long easyContractId,
                                            @Valid @RequestBody EasyContractFileRequestDto easyContractFileRequestDto) {
        EasyContractFileAttachResponseDto responseDto = easyContractService.attachFiles(memberId, easyContractId, easyContractFileRequestDto);
        return new DataResponseDto<>(Code.SUCCESS, "쉬운 계약서 파일이 성공적으로 첨부되었습니다.", responseDto);
    }

    @GetMapping("/{easyContractId}/assets")
    public DataResponseDto<EasyContractAssetListResponseDto> getEasyContractAssets(@CurrentMemberId Long memberId, @PathVariable Long easyContractId) {
        EasyContractAssetListResponseDto responseDto = easyContractService.getEasyContractAssets(memberId, easyContractId);
        return new DataResponseDto<>(Code.SUCCESS, "쉬운 계약서 첨부 파일 조회에 성공하였습니다.", responseDto);
    }

    @PatchMapping("/{easyContractId}")
    public DataResponseDto<EasyContractUpdateResponseDto> updateEasyContractTitle(@CurrentMemberId Long memberId, @PathVariable Long easyContractId,
                                                                                  @Valid @RequestBody EasyContractUpdateRequestDto requestDto) {
        EasyContractUpdateResponseDto responseDto = easyContractService.updateEasyContractTitle(memberId, easyContractId, requestDto);
        return new DataResponseDto<>(Code.SUCCESS, "쉬운 계약서 제목 수정에 성공하였습니다.", responseDto);
    }

    @DeleteMapping("/{easyContractId}")
    public ResponseEntity<Void> deleteEasyContract(@CurrentMemberId Long memberId, @PathVariable Long easyContractId) {
        easyContractService.deleteEasyContract(memberId, easyContractId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{easyContractId}/files/{fileAssetId}")
    public ResponseEntity<Void> deleteEasyContractFile(@CurrentMemberId Long memberId, @PathVariable Long easyContractId, @PathVariable Long fileAssetId) {
        easyContractService.deleteEasyContractFile(memberId, easyContractId, fileAssetId);
        return ResponseEntity.noContent().build();
    }
}
