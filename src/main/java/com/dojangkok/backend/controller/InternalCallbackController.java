package com.dojangkok.backend.controller;

import com.dojangkok.backend.common.dto.ResponseDto;
import com.dojangkok.backend.common.enums.Code;
import com.dojangkok.backend.dto.checklist.ChecklistCallbackRequestDto;
import com.dojangkok.backend.service.ChecklistTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/callbacks")
public class InternalCallbackController {

    private final ChecklistTemplateService checklistTemplateService;

    @PostMapping("/checklists/{templateId}/complete")
    public ResponseDto completeChecklistGeneration(@PathVariable Long templateId, @RequestBody ChecklistCallbackRequestDto requestDto) {
        log.info("Received checklist callback for templateId: {}", templateId);
        checklistTemplateService.completeChecklistGeneration(templateId, requestDto.getChecklists());
        return new ResponseDto(Code.SUCCESS, "체크리스트 생성 완료 처리되었습니다.");
    }
}
