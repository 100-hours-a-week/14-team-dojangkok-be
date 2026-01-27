package com.dojangkok.backend.controller;

import com.dojangkok.backend.auth.jwt.CurrentMemberId;
import com.dojangkok.backend.common.dto.DataResponseDto;
import com.dojangkok.backend.common.enums.Code;
import com.dojangkok.backend.dto.checklist.*;
import com.dojangkok.backend.dto.homenote.*;
import com.dojangkok.backend.service.ChecklistService;
import com.dojangkok.backend.service.HomeNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/home-notes")
public class HomeNoteController {

    private final ChecklistService checklistService;
    private final HomeNoteService homeNoteService;

    @PostMapping
    public DataResponseDto<HomeNoteCreateResponseDto> createHomeNote(@CurrentMemberId Long memberId,
                                                                     @Valid @RequestBody HomeNoteCreateRequestDto requestDto) {
        HomeNoteCreateResponseDto responseDto = homeNoteService.createHomeNote(memberId, requestDto);
        return new DataResponseDto<>(Code.CREATED_SUCCESS, "집 노트 생성에 성공하였습니다.", responseDto);
    }

    @GetMapping
    public DataResponseDto<HomeNoteListResponseDto> getHomeNoteList(@CurrentMemberId Long memberId,
                                                                    @RequestParam(required = false) String cursor) {
        HomeNoteListResponseDto responseDto = homeNoteService.getHomeNoteList(memberId, cursor);
        return new DataResponseDto<>(Code.SUCCESS, "집 노트 목록 조회에 성공하였습니다.", responseDto);
    }

    @GetMapping("/{homeNoteId}")
    public DataResponseDto<HomeNoteDetailResponseDto> getHomeNoteDetail(@CurrentMemberId Long memberId, @PathVariable Long homeNoteId,
                                                                        @RequestParam(required = false) String cursor) {
        HomeNoteDetailResponseDto responseDto = homeNoteService.getHomeNoteDetail(memberId, homeNoteId, cursor);
        return new DataResponseDto<>(Code.SUCCESS, "집 노트 상세 조회에 성공하였습니다.", responseDto);
    }

    @PatchMapping("/{homeNoteId}")
    public DataResponseDto<HomeNoteUpdateResponseDto> updateHomeNoteTitle(@CurrentMemberId Long memberId, @PathVariable Long homeNoteId,
                                                                          @Valid @RequestBody HomeNoteUpdateRequestDto requestDto) {
        HomeNoteUpdateResponseDto responseDto = homeNoteService.updateHomeNoteTitle(memberId, homeNoteId, requestDto);
        return new DataResponseDto<>(Code.SUCCESS, "집 노트 제목 수정에 성공하였습니다.", responseDto);
    }

    @DeleteMapping("/{homeNoteId}")
    public ResponseEntity<Void> deleteHomeNote(@CurrentMemberId Long memberId, @PathVariable Long homeNoteId) {
        homeNoteService.deleteHomeNote(memberId, homeNoteId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{homeNoteId}/files")
    public DataResponseDto<HomeNoteFileAttachResponseDto> attachFiles(@CurrentMemberId Long memberId, @PathVariable Long homeNoteId,
                                                                      @Valid @RequestBody HomeNoteFileAttachRequestDto requestDto) {
        HomeNoteFileAttachResponseDto responseDto = homeNoteService.attachFiles(memberId, homeNoteId, requestDto);
        return new DataResponseDto<>(Code.SUCCESS, "집 노트 이미지가 성공적으로 첨부되었습니다.", responseDto);
    }

    @DeleteMapping("/{homeNoteId}/file/{fileId}")
    public ResponseEntity<Void> deleteHomeNoteFile(@CurrentMemberId Long memberId, @PathVariable Long homeNoteId,
                                                   @PathVariable Long fileId) {
        homeNoteService.deleteHomeNoteFile(memberId, homeNoteId, fileId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/checklists/template")
    public DataResponseDto<ChecklistTemplateResponseDto> getChecklistTemplate(@CurrentMemberId Long memberId) {
        ChecklistTemplateResponseDto responseDto = checklistService.getChecklistTemplate(memberId);
        return new DataResponseDto<>(Code.SUCCESS, "체크리스트 템플릿 조회에 성공하였습니다.", responseDto);
    }

    @GetMapping("/{homeNoteId}/checklists")
    public DataResponseDto<ChecklistResponseDto> getChecklist(@CurrentMemberId Long memberId, @PathVariable Long homeNoteId) {
        ChecklistResponseDto responseDto = checklistService.getChecklist(memberId, homeNoteId);
        return new DataResponseDto<>(Code.SUCCESS, "체크리스트 조회에 성공하였습니다.", responseDto);
    }

    @PutMapping("/{homeNoteId}/checklists")
    public DataResponseDto<ChecklistUpdateResponseDto> updateChecklist(@CurrentMemberId Long memberId, @PathVariable Long homeNoteId,
                                                                       @Valid @RequestBody ChecklistUpdateRequestDto requestDto) {
        ChecklistUpdateResponseDto responseDto = checklistService.updateChecklist(memberId, homeNoteId, requestDto);
        return new DataResponseDto<>(Code.SUCCESS, "체크리스트 수정에 성공하였습니다.", responseDto);
    }

    @PatchMapping("/{homeNoteId}/checklists/items/{itemId}")
    public DataResponseDto<ChecklistItemStatusResponseDto> updateChecklistItemStatus(@CurrentMemberId Long memberId, @PathVariable Long homeNoteId, @PathVariable Long itemId,
                                                                                     @Valid @RequestBody ChecklistItemStatusRequestDto requestDto) {
        ChecklistItemStatusResponseDto responseDto = checklistService.updateChecklistItemStatus(memberId, homeNoteId, itemId, requestDto);
        return new DataResponseDto<>(Code.SUCCESS, "체크리스트 항목 상태 변경에 성공하였습니다.", responseDto);
    }
}
