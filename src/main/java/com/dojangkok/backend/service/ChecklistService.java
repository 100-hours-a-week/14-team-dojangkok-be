package com.dojangkok.backend.service;

import com.dojangkok.backend.common.enums.Code;
import com.dojangkok.backend.common.exception.GeneralException;
import com.dojangkok.backend.domain.*;
import com.dojangkok.backend.dto.checklist.*;
import com.dojangkok.backend.mapper.ChecklistMapper;
import com.dojangkok.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChecklistService {

    private final ChecklistRepository checklistRepository;
    private final ChecklistItemRepository checklistItemRepository;
    private final ChecklistTemplateRepository checklistTemplateRepository;
    private final ChecklistTemplateItemRepository checklistTemplateItemRepository;
    private final HomeNoteRepository homeNoteRepository;
    private final LifestyleRepository lifestyleRepository;
    private final ChecklistMapper checklistMapper;


    @Transactional(readOnly = true)
    public ChecklistTemplateResponseDto getChecklistTemplate(Long memberId) {
        ChecklistTemplate currentChecklistTemplate = getCurrentChecklistTemplate(memberId);
        List<ChecklistTemplateItem> templateItems = checklistTemplateItemRepository
                .findAllByChecklistTemplateId(currentChecklistTemplate.getId());

        return checklistMapper.toChecklistTemplateResponseDto(currentChecklistTemplate, templateItems);
    }

    @Transactional(readOnly = true)
    public ChecklistResponseDto getChecklist(Long memberId, Long homeNoteId) {
        HomeNote homeNote = getHomeNoteWithAccessCheck(memberId, homeNoteId);
        Checklist checklist = checklistRepository.findByHomeNoteId(homeNote.getId())
                .orElseThrow(() -> new GeneralException(Code.CHECKLIST_NOT_FOUND));

        List<ChecklistItem> items = checklistItemRepository.findAllByChecklistId(checklist.getId());

        return checklistMapper.toChecklistResponseDto(checklist, items);
    }

    /**
     * 집 노트 생성 시 체크리스트 초기화
     * - 체크리스트 생성
     * - 템플릿 아이템들을 체크리스트 아이템으로 복사
     * @return 생성된 체크리스트 응답 DTO
     */
    @Transactional
    public ChecklistResponseDto initializeChecklist(Member member, HomeNote homeNote) {
        ChecklistTemplate template = getCurrentChecklistTemplate(member.getId());
        Checklist checklist = Checklist.createChecklist(member, homeNote, template);
        checklistRepository.save(checklist);

        List<ChecklistTemplateItem> templateItems = checklistTemplateItemRepository
                .findAllByChecklistTemplateId(template.getId());

        List<ChecklistItem> checklistItems = templateItems.stream()
                .map(templateItem -> ChecklistItem.createChecklistItem(templateItem.getContent(), false, checklist))
                .toList();

        List<ChecklistItem> savedItems = checklistItemRepository.saveAll(checklistItems);

        return checklistMapper.toChecklistResponseDto(checklist, savedItems);
    }

    @Transactional
    public ChecklistUpdateResponseDto updateChecklist(Long memberId, Long homeNoteId, ChecklistUpdateRequestDto requestDto) {
        getHomeNoteWithAccessCheck(memberId, homeNoteId);

        Checklist checklist = checklistRepository.findByHomeNoteId(homeNoteId)
                .orElseThrow(() -> new GeneralException(Code.CHECKLIST_NOT_FOUND));

        checklistItemRepository.deleteAllByChecklistId(checklist.getId());

        List<ChecklistItem> newItems = requestDto.getChecklists().stream()
                .map(itemDto -> ChecklistItem.createChecklistItem(itemDto.getContent(), itemDto.isCompleted(), checklist))
                .toList();

        List<ChecklistItem> savedItems = checklistItemRepository.saveAll(newItems);

        return checklistMapper.toChecklistSaveResponseDto(checklist, savedItems);
    }

    @Transactional
    public ChecklistItemStatusResponseDto updateChecklistItemStatus(Long memberId, Long homeNoteId, Long itemId,
                                                                    ChecklistItemStatusRequestDto requestDto) {
        getHomeNoteWithAccessCheck(memberId, homeNoteId);

        Checklist checklist = checklistRepository.findByHomeNoteId(homeNoteId)
                .orElseThrow(() -> new GeneralException(Code.CHECKLIST_NOT_FOUND));

        ChecklistItem item = checklistItemRepository.findByIdAndChecklistId(itemId, checklist.getId())
                .orElseThrow(() -> new GeneralException(Code.CHECKLIST_ITEM_NOT_FOUND));

        item.updateStatus(requestDto.isCompleted());

        return checklistMapper.toChecklistItemStatusResponseDto(item);
    }


    private HomeNote getHomeNoteWithAccessCheck(Long memberId, Long homeNoteId) {
        HomeNote homeNote = homeNoteRepository.findById(homeNoteId)
                .orElseThrow(() -> new GeneralException(Code.HOME_NOTE_NOT_FOUND));

        if (!homeNote.getMember().getId().equals(memberId)) {
            throw new GeneralException(Code.HOME_NOTE_ACCESS_DENIED);
        }

        return homeNote;
    }

    private ChecklistTemplate getCurrentChecklistTemplate(Long memberId) {
        Lifestyle lifestyle = lifestyleRepository.findByMemberId(memberId)
                .orElseThrow(() -> new GeneralException(Code.LIFESTYLE_NOT_FOUND));

        return checklistTemplateRepository
                .findByLifestyleVersionId(lifestyle.getCurrentVersion().getId())
                .orElseThrow(() -> new GeneralException(Code.CHECKLIST_TEMPLATE_NOT_FOUND));
    }
}
